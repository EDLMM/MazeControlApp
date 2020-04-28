package com.remote;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;

import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.mazecontrol.Views.CellGroup;
import com.example.mazecontrol.Views.Spot_Location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;


public class MultiCastServiceSend extends Service {

    private static final String TAG = "Remote";
    private int startId;

    // for topology
    private static WorkThread workThread;
    private MulticastSocket mSocket;
    private InetAddress mAddress;
    private CellGroup cellGroup=null;

    //for location
    private static WorkThread_Location workThread_location;
    private MulticastSocket mSocket_location;
    private InetAddress mAddress_location;
    private Spot_Location spot_location=null;

    public MultiCastServiceSend(){

    }

    // 用 binder 来在 activity 和 service 之间通信
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MultiCastServiceSend getService() {
            // Return this instance of LocalService so clients can call public methods
            return MultiCastServiceSend.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    /** method for clients */
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }
    public CellGroup getServiceCellGroup(){
        return cellGroup;
    }
    public void setServiceCellGroup(CellGroup cellsFromView){
        cellGroup = cellsFromView;
    }
    public void setServiceSpotLocation(Spot_Location spotFromView){spot_location=spotFromView;}


    @Override
    public void onCreate() {
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
                        startSend();
                        break;
                    case UPDATE:
                        break;
                    case STOP:
                        stopSend() ;
                        break;
                }
            }
        }
        return super.onStartCommand(intent,flags,starId);
    }
    @Override
    public void onDestroy(){
        Log.e(TAG, "onDestroy");
        stopSend();
        super.onDestroy();
    }

    /**
     * 启动响应线程，收到设备搜索命令后，自动响应
     */
    public void startSend() {
        try {
            Log.e(TAG, "onCreate set socket");
            mAddress = InetAddress.getByName(UDPConstant.IP_ADDRESS);
            mAddress_location = InetAddress.getByName(UDPConstant.IP_ADDRESS);

            if (!mAddress.isMulticastAddress()) {
//                throw new NoMulticastException();
                Log.i(TAG, "run: " + "NOT MULTICAST ADDRESS!");
            }

            mSocket = new MulticastSocket(UDPConstant.PORT);
            mSocket.setTimeToLive(UDPConstant.TTLTIME);
//            mSocket.joinGroup(mAddress);
            mSocket_location = new MulticastSocket(UDPConstant.LOCATION_PORT);
            mSocket_location.setTimeToLive(UDPConstant.TTLTIME);
            //仅仅创建
//            new WorkThread().start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (workThread == null) {
            Log.d(TAG, "MultiCastServiceSend start");
            workThread = new WorkThread();
            workThread.start();
        }
        if (workThread_location == null) {
            Log.d(TAG, "MultiCastServiceSend for location start");
            workThread_location = new WorkThread_Location();
            workThread_location.start();
        }
    }
    /**
     * 停止响应
     */
    public void stopSend() {
        Log.d(TAG, "MultiCastServiceSend stop");
        mSocket.close();
        if (workThread != null) {
            Log.d(TAG, "MultiCastServiceSend kill work thread");
            workThread.destory();
            workThread = null;
        }
        mSocket_location.close();
        if (workThread_location != null) {
            Log.d(TAG, "MultiCastServiceSend kill work thread");
            workThread_location.destory();
            workThread_location = null;
        }
        stopSelf(startId);
    }

    private class WorkThread extends Thread {

        @Override
        public void run() {
            DatagramPacket datagramPacket = null;
            try {
                    Log.d(TAG, "MultiCastServiceSend Sending");
                    while (true) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();//程序内部创建一个byte型别数组的缓冲区，然后利用ByteArrayOutputStream和ByteArrayInputStream的实例向数组中写入或读出byte型数据
                        ObjectOutputStream oos = null;
                        cellGroup.message = "Message: " + Integer.toString(getRandomNumber());
                        try {
                            oos = new ObjectOutputStream(bos);
                            oos.writeObject(cellGroup);
                        //                oos.flush();
                            oos.close(); //从内存中直接释放，因为下次还要new
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG,"cell Gruop 序列化后字节数：" + bos.toByteArray().length);
                        byte[] data = bos.toByteArray();       //转化为字节数组
                        datagramPacket = new DatagramPacket(data, data.length, mAddress, UDPConstant.PORT);
                        mSocket.send(datagramPacket);
                        Log.i(TAG, "send: " + cellGroup.message + " to " + mAddress.getHostAddress());
                        Thread.sleep(UDPConstant.SEND_VIEW_UPDATA_INTERVAL_MS );
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
        }

        public void destory() {
            this.interrupt();
        }
    }

    private class WorkThread_Location extends Thread{
        @Override
        public void run() {
            DatagramPacket datagramPacket = null;
            try {
                Log.d(TAG, "MultiCastServiceSend location Sending");
                while (true) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();//程序内部创建一个byte型别数组的缓冲区，然后利用ByteArrayOutputStream和ByteArrayInputStream的实例向数组中写入或读出byte型数据
                    ObjectOutputStream oos = null;
                    try {
                        oos = new ObjectOutputStream(bos);
                        oos.writeObject(spot_location);
                        //                oos.flush();
                        oos.close(); //从内存中直接释放，因为下次还要new
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"spot Location 序列化后字节数：" + bos.toByteArray().length);
                    byte[] data = bos.toByteArray();       //转化为字节数组
                    datagramPacket = new DatagramPacket(data, data.length, mAddress_location, UDPConstant.LOCATION_PORT);
                    mSocket_location.send(datagramPacket);
                    Log.i(TAG, Integer.toString(getRandomNumber()) + "send Spot name:" + spot_location.getP_id() + " to " + mAddress_location.getHostAddress());
                    Thread.sleep(UDPConstant.SEND_VIEW_UPDATA_INTERVAL_MS );
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void destory() {
            this.interrupt();
        }
    }


}
