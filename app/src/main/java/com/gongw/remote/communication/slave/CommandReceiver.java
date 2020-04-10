package com.gongw.remote.communication.slave;

import android.util.Log;

import com.gongw.remote.RemoteConst;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.host.Command;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * 用于接收命令和回写应答
 * Created by gw on 2017/11/6.
 */
public class CommandReceiver {

    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(7, 8, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ReceiveCommandThreadFactory(), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new RejectedExecutionException();
        }
    });
    private static CommandListener listener;
    private static volatile boolean isOpen;


    public static void open(CommandListener commandListener){
        listener = commandListener;
        isOpen = true;
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(RemoteConst.COMMAND_RECEIVE_PORT);
                    while(isOpen){
                        Socket socket = serverSocket.accept();
                        threadPool.execute(new CommandParseRunnable(socket));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void close(){
        isOpen = false;
        threadPool.shutdown();
    }

    public static class CommandParseRunnable implements Runnable{
        Socket socket;

        public CommandParseRunnable(Socket socket){
            this.socket = socket;
        }

//        @Override
//        public void run() {
//            ObjectOutputStream os = null;
//            ObjectInputStream is = null;
//            try {
//                Command admin,user;
//                is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
//                Object obj = is.readObject();
//                if (obj != null) {
//                    admin = (Command) obj;
//                    // 通过这个 interface 把网络层反馈的string传递出来
//                    listener.onReceive(admin.getContent());
//                    Log.d("Remote",String.format("player receive: %s",admin.getContent()));
////                    System.out.println("user: " + user.getName() + "/" + user.getPassword());
//                }
//
//                Log.d("Remote",String.format("start Receiver"));
//                os = new ObjectOutputStream(socket.getOutputStream());
//
//                user = new Command("Send by player",null);
//                os.writeObject(user);
//                os.flush();
//
//
//            } catch(IOException | ClassNotFoundException ex) {
////                logger.log(Level.SEVERE, null, ex);
//            } finally {
//                try {
//                    is.close();
//                } catch(Exception ex) {}
//                try {
//                    os.close();
//                } catch(Exception ex) {}
//                try {
//                    socket.close();
//                } catch(Exception ex) {}
//            }
//        }

        @Override
        public void run() {
            try {
                DataInputStream is = new DataInputStream(socket.getInputStream());
                OutputStream os = socket.getOutputStream();
                byte[] bytes = new byte[1024*8];
                int i=0;
                while(true){
                    bytes[i] = (byte) is.read();
                    if (bytes[i] == -1) {
                        break;
                    }
                    if((char)bytes[i] != CommunicationKey.EOF.charAt(0)){
                        i++;
                    }else{
                        String command = new String(bytes, 0, i+1, Charset.defaultCharset()).replace(CommunicationKey.EOF, "");
                        if(listener!=null){
                            os.write((listener.onReceive(command)).getBytes());
                        }else{
                            os.write((CommunicationKey.RESPONSE_OK+ CommunicationKey.EOF).getBytes());
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

    }

    public interface CommandListener{
        String onReceive(String msg);
    }
}
