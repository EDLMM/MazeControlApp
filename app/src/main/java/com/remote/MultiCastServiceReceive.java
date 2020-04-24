package com.remote;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.mazecontrol.Views.CellGroup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class MultiCastServiceReceive extends IntentService {

    private static final String TAG = "Remote";
    private static WorkThread workThread = null;
    private boolean isScoketOpen=false;
    private MulticastSocket mSocket;
    private InetAddress mAddress;
    private int startId;

    private CellGroup cellGroup=null;

    public MultiCastServiceReceive(){
        super(MultiCastServiceReceive.class.getSimpleName());
    }

    // 用 binder 来在 activity 和 service 之间通信
    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MultiCastServiceReceive getService() {
            // Return this instance of LocalService so clients can call public methods
            return MultiCastServiceReceive.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public CellGroup getServiceCellGroup(){
        return cellGroup;
    }
    public void setServiceCellGroup(CellGroup cellsFromView){
        cellGroup = cellsFromView;
    }
    public boolean isCellGroupInitialized(){
        if (cellGroup == null){
            return false;
        }else{
            return true;
        }
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG,"handle intent func");
        sendCellUpdateBroadCast();
    }

    /**
     * This method is responsible to send broadCast to specific Action
     * */
    private void sendCellUpdateBroadCast() {
        try
        {
            Log.d(TAG,"sendCellUpdateBroadCast()");
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(UDPConstant.RECEIVE_UPDATE_BROADCAST_ACTION);

            // uncomment this line if you want to send data
//            broadCastIntent.putExtra("data", "abc");
            sendBroadcast(broadCastIntent);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int starId){
        this.startId = startId;
        Log.e(TAG, "onStartCommand---startId: " + startId);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            UDPConstant.Control control = (UDPConstant.Control) bundle.getSerializable("Key");
            if (control != null) {
                switch (control) {
                    case PLAY:
                        startReceive();
                        break;
                    case UPDATE:
                        break;
                    case STOP:
                        stopReceive() ;
                        break;
                }
            }
        }
        return super.onStartCommand(intent,flags,starId);
    }

    @Override
    public void onDestroy(){
        Log.e(TAG, "onDestroy");
        stopReceive();
        super.onDestroy();
    }

    /**
     * 启动响应线程，收到设备搜索命令后，自动响应
     */
    public void startReceive() {
        isScoketOpen=true;
        try {
            mAddress = InetAddress.getByName(UDPConstant.IP_ADDRESS);

            if (!mAddress.isMulticastAddress()) {
//                throw new NoMulticastException();
                Log.i(TAG, "run: " + "NOT MULTICAST ADDRESS!");
            }
            mSocket = new MulticastSocket(UDPConstant.PORT);
            mSocket.setTimeToLive(UDPConstant.TTLTIME);
            mSocket.joinGroup(mAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //开始下载
        if (workThread == null) {
            Log.d(TAG, "MultiCastServiceReceive start listen");
            workThread = new WorkThread();
            workThread.start();
        }
    }
    /**
     * 停止响应
     */
    public void stopReceive() {
        Log.d(TAG, "stopReceive()");
        isScoketOpen=false;
        //获取进度
        mSocket.close();
        if (workThread != null) {
            Log.d(TAG, "MultiCastServiceReceive stop listen");
            workThread.destory();
            workThread = null;
        }
        stopSelf(startId);
    }


    private class WorkThread extends Thread {

        @Override
        public void run() {
            byte[] buffer = new byte[UDPConstant.CELL_GROUP_SERIALIZABLE_BUF];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, UDPConstant.CELL_GROUP_SERIALIZABLE_BUF);
            while (isScoketOpen) {

                try {
                    mSocket.receive(datagramPacket);
                    ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(buffer));
                    cellGroup = (CellGroup) iStream.readObject();
                    iStream.close();
                    String result = cellGroup.message ;
                    Log.d(TAG, "receive: " + result);
                    // 让 activity 更新 view
                    sendCellUpdateBroadCast();
                } catch (IOException | ClassNotFoundException e) {
                    Log.d(TAG, "IOException | ClassNotFoundException");
                    e.printStackTrace();
                }
            }
        }

        public void destory() {
            this.interrupt();
        }
    }

}
