package com.example.mazecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import com.gongw.remote.Device;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.communication.host.Command;
import com.gongw.remote.communication.host.CommandSender;
import com.gongw.remote.search.DeviceSearcher;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AdminHostActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    // CustomView, i.e. random maze generation
    private CustomView randomMazeGame;

//    DeviceSearcher DeviceSearcher=new DeviceSearcher();

    private List<Device> deviceList = new ArrayList<>();
//    private SimpleAdapter<Device> adapter;

    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,AdminHostActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
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

    }

    public void onClickReGenerateMaze(View view){
        randomMazeGame.onClickReGeneration();
        Log.d("Remote","onClickReGenerateMaze");

    }

    public void onClickSearchPlayer(View view){
        Log.d("Remote","onClickSearchPlayer");
        // 搜索设备
        // BUG 华为手机作为管理端无法搜索到小米手机，小米手机可以做管理端
        startSearch();
    }

    public void onClickSendAlert(View view){
        Log.d("Remote","onClickSendAlert");
        // 搜索设备
        for(int i=0; i<deviceList.size(); i++){
            sendCommand(deviceList.get(i));
        }
    }


    private void initTopBar() {
        mTopBar.setTitle("管理员界面");
    }

    /**
     * 开始异步搜索局域网中的设备
     */
    private void startSearch(){
        DeviceSearcher.search(new DeviceSearcher.OnSearchListener() {
            @Override
            public void onSearchStart() {
//                binding.srlRefreshLayout.setRefreshing(true);
                Toast.makeText(AdminHostActivity.this, "管理端开始搜索玩家设备", Toast.LENGTH_SHORT).show();
                deviceList.clear();
//                deviceList.add(new Device("10.0.0.8", RemoteConst.DEVICE_SEARCH_PORT,"mi8"));
            }

            @Override
            public void onSearchedNewOne(Device device) {
//                binding.srlRefreshLayout.setRefreshing(false);
//                randomMazeGame.onClickReGeneration();
                Toast.makeText(AdminHostActivity.this, "搜索到玩家设备", Toast.LENGTH_SHORT).show();
                deviceList.add(device);
//                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSearchFinish() {
//                binding.srlRefreshLayout.setRefreshing(false);
//                adapter.notifyDataSetChanged();
                Toast.makeText(AdminHostActivity.this, "管理端完成搜索玩家设备", Toast.LENGTH_SHORT).show();
                Log.d("Remote","finish searching, player list:");
                for(int i=0; i<deviceList.size(); i++){
                    Log.d("Remote",deviceList.get(i).getIp());
                }
            }
        });
    }

    private void sendCommand(Device device){
        //发送命令，命令内容为"hello!"
        //UI接收网络层的相应
        Command command = new Command("时间限时提醒", new Command.Callback() {
            @Override
            public void onRequest(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminHostActivity.this, "已发送 时间限时提醒", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminHostActivity.this, "玩家设备在线确认 "+msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminHostActivity.this, "Error:"+msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onEcho(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminHostActivity.this, "Echo："+msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        command.setDestIp(device.getIp());
        CommandSender.addCommand(command);
    }
}