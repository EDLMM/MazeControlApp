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

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerSlaveActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    // CustomView, i.e. random maze generation
    private CustomView randomMazeGame;
    private static final String TAG = "Remote" ;
    private static String device_id ="player_"+ Build.PRODUCT+ "_" + Build.ID;


    MyBroadCastReceiver myBroadCastReceiver;

    /* 是否开启了搜索响应和通信响应*/
    private boolean isOpen=false;

    MultiCastServiceReceive mServiceReceive; //绑定服务
    boolean mBoundReceive = false; //服务绑定变了

    MultiCastServiceSend mServiceSend; //绑定服务
    boolean mBoundSend = false; //服务绑定变了

    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,PlayerSlaveActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intentSend = new Intent(this, MultiCastServiceSend.class);
        bindService(intentSend, connectionSend, Context.BIND_AUTO_CREATE);
        // Bind to LocalService
        Intent intent = new Intent(this, MultiCastServiceReceive.class);
        bindService(intent, connectionReceive, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_player_slave, null);
        ButterKnife.bind(this, root);
        mTopBar.setTitle("玩家界面");
        setContentView(root);

        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock("mylock");
            lock.acquire();
        }

        randomMazeGame =(CustomView) findViewById(R.id.random_maze_game);
        randomMazeGame.setVisibility(View.INVISIBLE); //hide temporarily
        // update the serializable cell group in background service once the view is changed
        randomMazeGame.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mBoundSend) {
                    // Call a method from the LocalService.
                    // However, if this call were something that might hang, then this request should
                    // occur in a separate thread to avoid slowing down the activity performance.
                    mServiceSend.setServiceSpotLocation(randomMazeGame.getSelfSpotLocation(device_id));
                    mServiceSend.setIsLocationChangedTrue();
                    randomMazeGame.invalidate();
                }
                return false;
            }
        });

        myBroadCastReceiver = new MyBroadCastReceiver();
        registerMyReceiver();
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();

        unbindService(connectionSend);
        mBoundSend = false;
        stopSendServiceLoca();

        // make sure to unregister your receiver after finishing of this activity
        unregisterReceiver(myBroadCastReceiver);
        unbindService(connectionReceive);
        mBoundReceive = false;
        stopReceiveServiceTopo();

    }

    /**
     * This method is responsible to register an action to BroadCastReceiver
     * */
    private void registerMyReceiver() {

        try
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UDPConstant.RECEIVE_TOPOLOGY_UPDATE_BROADCAST_ACTION);
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
                Log.d(TAG, "onReceive() called");

                if ( randomMazeGame != null)
                {
                    if(mServiceReceive. isCellGroupNotNull()) {
                        Log.d(TAG, "update view");
                        //TODO change 变成只改topo 不改loca
                        randomMazeGame.setTopoByCellGroup(mServiceReceive.getServiceCellGroup());
                        randomMazeGame.invalidate();
                        randomMazeGame.setVisibility(View.VISIBLE);
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public void onClickSwitchReponseStat(View v) {

        if(isOpen){
            stopSendServiceLoca();
            stopReceiveServiceTopo();
            isOpen = false;
            Toast.makeText(PlayerSlaveActivity.this, "已经关闭游戏连线！", Toast.LENGTH_SHORT).show();
        }else{
            startSendServiceLoca();
            startReceiveServiceTopo();
            isOpen = true;
            Toast.makeText(PlayerSlaveActivity.this, "已经打开响应程序！", Toast.LENGTH_SHORT).show();
        }
    }
    private void startSendServiceLoca(){
        Intent intent = new Intent(this, MultiCastServiceSend.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Key", UDPConstant.Control.LOCA_START);
        intent.putExtras(bundle);
        startService(intent);
    }
    private void stopSendServiceLoca() {
//        Intent intent = new Intent(this, MultiCastServiceSend.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("Key", UDPConstant.Control.LOCA_STOP);
//        intent.putExtras(bundle);
//        startService(intent);
        Intent intent = new Intent(this, MultiCastServiceSend.class);
        stopService(intent);
    }
    private void startReceiveServiceTopo(){
        Intent intent = new Intent(this, MultiCastServiceReceive.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Key", UDPConstant.Control.TOPO_START);
        intent.putExtras(bundle);
        startService(intent);
    }

    private void stopReceiveServiceTopo() {
//        Intent intent = new Intent(this, MultiCastServiceReceive.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("Key", UDPConstant.Control.TOPO_STOP);
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
//            mServiceSend.setServiceCellGroup(randomMazeGame.getCells());
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
            mServiceReceive.setServiceCellGroup(randomMazeGame.getCells());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundReceive = false;
        }
    };
}