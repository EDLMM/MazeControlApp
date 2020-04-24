package com.example.mazecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.mazecontrol.Views.CustomView;
import com.example.mazecontrol.Views.HostMazeView;
import com.gongw.remote.Device;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.communication.host.Command;
import com.gongw.remote.communication.host.CommandSender;
import com.gongw.remote.search.DeviceSearcher;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.remote.MazeTopoCommand;
import com.remote.MultiCastServiceSend;
import com.remote.UDPConstant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AdminHostActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    // CustomView, i.e. random maze generation
    private CustomView randomMazeGame;
    private MazeTopoCommand mazeCommand = new MazeTopoCommand();

    //用于单次发送
    private MulticastSocket mSocket;
    private int send_count=0;

    MultiCastServiceSend mService; //绑定服务
    boolean mBound = false; //服务绑定变了

    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,AdminHostActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, MultiCastServiceSend.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_admin_host, null);
        ButterKnife.bind(this, root);
        initTopBar();
        setContentView(root);

        randomMazeGame =(CustomView) findViewById(R.id.random_maze_game);

        // update the serializable cell group in background service once the view is changed
        randomMazeGame.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mBound) {
                    // Call a method from the LocalService.
                    // However, if this call were something that might hang, then this request should
                    // occur in a separate thread to avoid slowing down the activity performance.
                    mService.setServiceCellGroup(randomMazeGame.getCells());
                }
                return false;
            }
        });

        //开启组播
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock("mylock");
            lock.acquire();
        }
    }
    private void initTopBar() {
        mTopBar.setTitle("管理员界面");
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
        unbindService(connection);
        mBound = false;
        stopSendService();
    }

    public void onClickReGenerateMaze(View view){
        randomMazeGame.onClickReGeneration();
        Log.d("Remote","onClickReGenerateMaze");
    }

    public void onClickSearchPlayer(View view) throws IOException, ClassNotFoundException {
        Log.d("Remote","onClick Send one message");
        // 搜索设备
        new SendThread().start();

    }


    private class SendThread extends Thread {
        @Override
        public void run() {

            try {
                mSocket = new MulticastSocket(UDPConstant.PORT);
                mSocket.setTimeToLive(UDPConstant.TTLTIME);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mBound) {
                // Call a method from the LocalService.
                // However, if this call were something that might hang, then this request should
                // occur in a separate thread to avoid slowing down the activity performance.
                send_count = mService.getRandomNumber();
            }

            DatagramPacket datagramPacket = null;
            byte[] data = ("CLICK ONE TIME SEND " + Integer.toString(send_count++)).getBytes();
            try {
                Log.d("Remote","Send one time");
                InetAddress address = InetAddress.getByName(UDPConstant.IP_ADDRESS);
//                if (!address.isMulticastAddress()) {
//                    throw new NoMulticastException();
//                }
                datagramPacket = new DatagramPacket(data, data.length, address, UDPConstant.PORT);
                mSocket.send(datagramPacket);
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isOpen=false;

    public void onClickSendAlert(View view){
        Log.d("Remote","onClickSendAlert");
        if(isOpen){
            //停止响应搜索
            stopSendService();
            isOpen = false;
            Toast.makeText(AdminHostActivity.this, "停止定时发送", Toast.LENGTH_SHORT).show();
        }else{
            //开始响应搜索
            startSendService();
            isOpen = true;
            Toast.makeText(AdminHostActivity.this, "开始定时发送", Toast.LENGTH_SHORT).show();
        }
    }


    private void startSendService(){
        Intent intent = new Intent(this, MultiCastServiceSend.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Key", UDPConstant.Control.PLAY);
        intent.putExtras(bundle);
        startService(intent);
    }
    private void stopSendService() {
//        Intent intent = new Intent(this, MultiCastServiceSend.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("Key", UDPConstant.Control.STOP);
//        intent.putExtras(bundle);
//        startService(intent);
        Intent intent = new Intent(this, MultiCastServiceSend.class);
        stopService(intent);
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MultiCastServiceSend.LocalBinder binder = (MultiCastServiceSend.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            // initialize the cell group in background service
            mService.setServiceCellGroup(randomMazeGame.getCells());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}