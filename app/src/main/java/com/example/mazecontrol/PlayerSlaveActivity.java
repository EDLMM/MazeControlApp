package com.example.mazecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.mazecontrol.Views.CustomView;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.remote.MultiCastServiceReceive;
import com.remote.UDPConstant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerSlaveActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    // CustomView, i.e. random maze generation
    private CustomView randomMazeGame;
    private static final String TAG = "Remote" ;
    MyBroadCastReceiver myBroadCastReceiver;

    /* 是否开启了搜索响应和通信响应*/
    private boolean isOpen=false;

    MultiCastServiceReceive mService; //绑定服务
    boolean mBound = false; //服务绑定变了

    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,PlayerSlaveActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, MultiCastServiceReceive.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_player_slave, null);
        ButterKnife.bind(this, root);
        mTopBar.setTitle("玩家界面");
        setContentView(root);

        randomMazeGame =(CustomView) findViewById(R.id.random_maze_game);
        randomMazeGame.setVisibility(View.INVISIBLE); //hide temporarily
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock("mylock");
            lock.acquire();
        }

        myBroadCastReceiver = new MyBroadCastReceiver();
        registerMyReceiver();
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
                    if(mService.isCellGroupInitialized() ) {
                        Log.d(TAG, "update view");
                        randomMazeGame.setAllByCellGroup(mService.getServiceCellGroup());
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
            stopReceiveService();
            isOpen = false;
            Toast.makeText(PlayerSlaveActivity.this, "已经关闭游戏连线！", Toast.LENGTH_SHORT).show();
        }else{
            startReceiveService();
            isOpen = true;
            Toast.makeText(PlayerSlaveActivity.this, "已经打开响应程序！", Toast.LENGTH_SHORT).show();
        }
    }

    private void startReceiveService(){
        Intent intent = new Intent(this, MultiCastServiceReceive.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Key", UDPConstant.Control.PLAY);
        intent.putExtras(bundle);
        startService(intent);
    }

    private void stopReceiveService() {
        Intent intent = new Intent(this, MultiCastServiceReceive.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Key", UDPConstant.Control.STOP);
        intent.putExtras(bundle);
        startService(intent);
//        Intent intent = new Intent(this, MultiCastServiceReceive.class);
//        stopService(intent);
    }


    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();

        // make sure to unregister your receiver after finishing of this activity
        unregisterReceiver(myBroadCastReceiver);

        unbindService(connection);
        mBound = false;

        stopReceiveService();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MultiCastServiceReceive.LocalBinder binder = (MultiCastServiceReceive.LocalBinder) service;
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