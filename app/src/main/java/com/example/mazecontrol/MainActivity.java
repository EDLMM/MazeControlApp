package com.example.mazecontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化状态栏
        QMUIStatusBarHelper.translucent(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        ButterKnife.bind(this, root);
        initTopBar();

        setContentView(root);
//        if (getIntent().getBooleanExtra("test_activity", false)) {
//            Toast.makeText(this, "恢复到最近阅读(Boolean)", Toast.LENGTH_SHORT).show();
//        }
//        setContentView(R.layout.activity_main);
    }

    public void onClickShowMazeActivity(View view){
        ShowMazeActivity.actionStart(this,"start from mainAct");
    }

    public void onClickAdminHostActivity(View view){
        AdminHostActivity.actionStart(this,"start from mainAct");
    }

    public void onClickPlayerSlaveActivity(View view){
        PlayerSlaveActivity.actionStart(this,"start from mainAct");
    }

    private void initTopBar() {
//        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
////                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
//            }
//        });

        mTopBar.setTitle("主页面");
    }



}
