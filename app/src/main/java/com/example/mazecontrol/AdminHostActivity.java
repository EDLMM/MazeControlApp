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

    private List<Device> deviceList = new ArrayList<>();
//    private SimpleAdapter<Device> adapter;

    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,AdminHostActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
    }
    // to remote test
    private ImageView iv_canvas;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    // CustomView, i.e. random maze generation
    private CustomView randomMazeGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_show_maze, null);
        ButterKnife.bind(this, root);
        initTopBar();
        setContentView(root);

        randomMazeGame =(CustomView) findViewById(R.id.random_maze_game);

    }

    public void onClickReGenerateMaze(View view){
        randomMazeGame.onClickReGeneration();
        Log.d("AdminHostActivity","onClickReGenerateMaze");

    }

    public void onClickSearchPlayer(View view){
        Log.d("AdminHostActivity","onClickSearchPlayer");
        // 搜索设备
        startSearch();
    }

    public void onClickSendAlert(View view){
        Log.d("AdminHostActivity","onClickSendAlert");
        // 搜索设备
        for(int i=0; i<deviceList.size(); i++){
            sendCommand(deviceList.get(i));
        }
    }


    private void initTopBar() {
//        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
////                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
//            }
//        });

        mTopBar.setTitle("展示迷宫");
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
            }

            @Override
            public void onSearchedNewOne(Device device) {
//                binding.srlRefreshLayout.setRefreshing(false);
                Toast.makeText(AdminHostActivity.this, "搜索到玩家设备", Toast.LENGTH_SHORT).show();
                deviceList.add(device);
//                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSearchFinish() {
//                binding.srlRefreshLayout.setRefreshing(false);
//                adapter.notifyDataSetChanged();
                Toast.makeText(AdminHostActivity.this, "管理端完成搜索玩家设备", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendCommand(Device device){
        //发送命令，命令内容为"hello!"
        Command command = new Command("Are you OK!", new Command.Callback() {
            @Override
            public void onRequest(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminHostActivity.this, "Request: Are you OK!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminHostActivity.this, "Success:"+msg, Toast.LENGTH_SHORT).show();
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
