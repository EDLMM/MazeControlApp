package com.example.mazecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.example.mazecontrol.Views.CustomView;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import com.remote.MultiCastServiceReceive;
import com.remote.MultiCastServiceSend;
import com.remote.UDPConstant;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AdminHostActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    // CustomView, i.e. random maze generation
    private CustomView randomMazeGame;
    private static String TAG ="Remote";
    private static String device_id="admin_"+ Build.PRODUCT+ "_" + Build.ID;

    //用于单次发送
    private MulticastSocket mSocket;
    private int send_count=0;

    MultiCastServiceSend mServiceSend; //绑定服务
    boolean mBoundSend = false; //服务绑定变了

    MultiCastServiceReceive mServiceReceive; //绑定服务
    boolean mBoundReceive = false; //服务绑定变了

    MyBroadCastReceiver myBroadCastReceiver;

    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,AdminHostActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intentSend = new Intent(this, MultiCastServiceSend.class);
        bindService(intentSend, connectionSend, Context.BIND_AUTO_CREATE);

        Intent intentReceive = new Intent(this, MultiCastServiceReceive.class);
        bindService(intentReceive, connectionReceive, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_admin_host, null);
        ButterKnife.bind(this, root);
        initTopBar();
        setContentView(root);

        //开启组播
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock("mylock");
            lock.acquire();
        }

        randomMazeGame =(CustomView) findViewById(R.id.random_maze_game);
        randomMazeGame.setAdminRole();
        // update the serializable cell group in background service once the view is changed
        randomMazeGame.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mBoundSend) {
                    // Call a method from the LocalService.
                    // However, if this call were something that might hang, then this request should
                    // occur in a separate thread to avoid slowing down the activity performance.
                    mServiceSend.setServiceCellGroup(randomMazeGame.getCells());
                    mServiceSend.setIsTopologyChangedTrue();
                }
                return false;
            }
        });
        myBroadCastReceiver = new MyBroadCastReceiver();
        registerMyReceiver();
    }
    private void initTopBar() {
        mTopBar.setTitle("管理员界面");
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
        unbindService(connectionSend);
        mBoundSend = false;
        stopSendServiceTopo();
        unbindService(connectionReceive);
        mBoundReceive = false;
        stopReceiveServiceLoca();
        // make sure to unregister your receiver after finishing of this activity
        unregisterReceiver(myBroadCastReceiver);

    }

    public void onClickReGenerateMaze(View view){
        randomMazeGame.onClickReGeneration();
        Log.d("Remote","onClickReGenerateMaze");
    }

    public void onClickSendOne(View view) throws IOException, ClassNotFoundException {
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

            if (mBoundSend) {
                // Call a method from the LocalService.
                // However, if this call were something that might hang, then this request should
                // occur in a separate thread to avoid slowing down the activity performance.
                send_count = mServiceSend.getRandomNumber();
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
    public void onClickMazeSendSwitch(View view){
        Log.d("Remote","onClickMazeSendSwitch");
        if(isOpen){
            //停止响应搜索
            stopSendServiceTopo();
            stopReceiveServiceLoca();
            isOpen = false;
            Toast.makeText(AdminHostActivity.this, "停止同步收发", Toast.LENGTH_SHORT).show();
        }else{
            //开始响应搜索
            startSendServiceTopo();
            startReceiveServiceLoca();
            isOpen = true;
            Toast.makeText(AdminHostActivity.this, "开始同步收发", Toast.LENGTH_SHORT).show();
        }
    }


    private void startSendServiceTopo(){
        Intent intent = new Intent(this, MultiCastServiceSend.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Key", UDPConstant.Control.TOPO_START);
        intent.putExtras(bundle);
        startService(intent);
    }
    private void stopSendServiceTopo() {
//        Intent intent = new Intent(this, MultiCastServiceSend.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("Key", UDPConstant.Control.TOPO_STOP);
//        intent.putExtras(bundle);
//        startService(intent);
        Intent intent = new Intent(this, MultiCastServiceSend.class);
        stopService(intent);
    }

    private void startReceiveServiceLoca(){
        Intent intent = new Intent(this, MultiCastServiceReceive.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Key", UDPConstant.Control.LOCA_START);
        intent.putExtras(bundle);
        startService(intent);
    }
    private void stopReceiveServiceLoca() {
//        Intent intent = new Intent(this, MultiCastServiceReceive.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("Key", UDPConstant.Control.LOCA_STOP);
//        intent.putExtras(bundle);
//        startService(intent);
        Intent intent = new Intent(this, MultiCastServiceReceive.class);
        stopService(intent);
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connectionSend = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MultiCastServiceSend.LocalBinder binder = (MultiCastServiceSend.LocalBinder) service;
            mServiceSend = binder.getService();
            mBoundSend = true;
            // initialize the cell group in background service
            mServiceSend.setServiceCellGroup(randomMazeGame.getCells());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundSend = false;
        }
    };
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connectionReceive = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MultiCastServiceReceive.LocalBinder binder = (MultiCastServiceReceive.LocalBinder) service;
            mServiceReceive = binder.getService();
            mBoundReceive = true;
            // initialize the cell group in background service
//            mServiceReceive.setServiceCellGroup(randomMazeGame.getCells());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundSend = false;
        }
    };
    /**
     * This method is responsible to register an action to BroadCastReceiver
     * */
    private void registerMyReceiver() {

        try
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UDPConstant.RECEIVE_LOCATION_UPDATE_BROADCAST_ACTION);
            registerReceiver(myBroadCastReceiver, intentFilter);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    /**
     * MyBroadCastReceiver is responsible to receive broadCast from register action
     * */
    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                Log.d(TAG, "onReceive() called from Admin Activity");

                if(mServiceReceive. isSpotLocationNotNull()) {
                    Log.d(TAG, "update adimn view");
                    randomMazeGame.setLocaBySpotLocation(mServiceReceive.getServiceSpotLocation());
                    randomMazeGame.invalidate();
                }

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}