package com.example.mazecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.mazecontrol.Views.CustomView;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.slave.CommandReceiver;
import com.gongw.remote.search.DeviceSearchResponser;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerSlaveActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    // CustomView, i.e. random maze generation
    private CustomView randomMazeGame;


    /**
     * 是否开启了搜索响应和通信响应
     */
    private boolean isOpen=false;

    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,PlayerSlaveActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_player_slave, null);
        ButterKnife.bind(this, root);
        initTopBar();
        setContentView(root);

    }

    private void initTopBar() {
        mTopBar.setTitle("玩家界面");
    }

    public void onClickSwitchReponseStat(View v) {

        if(isOpen){
            //停止响应搜索
            DeviceSearchResponser.close();
            isOpen = false;
//            binding.btnOpenResponser.setText("打开应答");
            //停止接收通信命令
            CommandReceiver.close();
//            Toast.makeText(PlayerSlaveActivity.this, "已经关闭响应程序！", Toast.LENGTH_SHORT).show();
            Toast.makeText(PlayerSlaveActivity.this, "已经关闭游戏连线！", Toast.LENGTH_SHORT).show();
        }else{
            //开始响应搜索
            DeviceSearchResponser.open();
            isOpen = true;
//            binding.btnOpenResponser.setText("关闭应答");
            //开始接受通信命令
            CommandReceiver.open(new CommandReceiver.CommandListener() {
                // 通过这个 interface 把网络层反馈的string传递出来
                @Override
                public String onReceive(final String msg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlayerSlaveActivity.this, "来自管理端 "+msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return CommunicationKey.RESPONSE_OK +
                            "OK" +
                            CommunicationKey.EOF;
                }
            });
            Toast.makeText(PlayerSlaveActivity.this, "已经打开游戏连线！", Toast.LENGTH_SHORT).show();
//            Toast.makeText(PlayerSlaveActivity.this, "已经打开响应程序！", Toast.LENGTH_SHORT).show();
        }
    }
}
