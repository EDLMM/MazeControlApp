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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;


public class ShowMazeActivity extends AppCompatActivity {


    public static void actionStart(Context context, String data1){
        Intent intent=new Intent(context,ShowMazeActivity.class );
        intent.putExtra("param1",data1);
        context.startActivity(intent);
    }
    // to remote
    private ImageView iv_canvas;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_maze);
        QMUIStatusBarHelper.translucent(this);

        // 初始化一个画笔，笔触宽度为5，颜色为红色
        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(Color.DKGRAY);

        iv_canvas = (ImageView) findViewById(R.id.iv_canvas);
//        btn_save = (Button) findViewById(R.id.btn_save);
//        btn_resume = (Button) findViewById(R.id.btn_resume);
//
//        btn_save.setOnClickListener(click);
//        btn_resume.setOnClickListener(click);
        iv_canvas.setOnTouchListener(touch);

    }
    private View.OnTouchListener touch = new OnTouchListener() {

        // 定义手指开始触摸的坐标
        float startX;
        float startY;


        // 画画程序
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                // 用户按下动作
                case MotionEvent.ACTION_DOWN:
                    // 第一次绘图初始化内存图片，指定背景为白色
                    if (baseBitmap == null) {
                        baseBitmap = Bitmap.createBitmap(iv_canvas.getWidth(),
                                iv_canvas.getHeight(), Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(baseBitmap);
                        canvas.drawColor(Color.WHITE);
                    }
                    // 记录开始触摸的点的坐标
                    startX = event.getX();
                    startY = event.getY();

                    String log_axis= String.format("touch down, x: %f, y:%f",startX,startY);
                    Log.d("ShowMazeActivity",log_axis);
                    break;
                // 用户手指在屏幕上移动的动作
                case MotionEvent.ACTION_MOVE:
                    // 记录移动位置的点的坐标
                    float stopX = event.getX();
                    float stopY = event.getY();

                    //根据两点坐标，绘制连线
                    canvas.drawLine(startX, startY, stopX, stopY, paint);

                    // 更新开始点的位置
                    startX = event.getX();
                    startY = event.getY();

                    // 把图片展示到ImageView中
                    iv_canvas.setImageBitmap(baseBitmap);
                    break;
                case MotionEvent.ACTION_UP:

                    break;
                default:
                    break;
            }
            return true;
        }
    };
    /**
     * 清除画板
     */
    public void resumeCanvas(View view) {
        // 手动清除画板的绘图，重新创建一个画板
        if (baseBitmap != null) {
            Log.d("ShowMazeActivity","run resumeCanvas(View view)");
            baseBitmap = Bitmap.createBitmap(iv_canvas.getWidth(),
                    iv_canvas.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
            canvas.drawColor(Color.WHITE);
            iv_canvas.setImageBitmap(baseBitmap);


//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            Toast.makeText(ShowMazeActivity.this, "清除画板成功，可以重新开始绘图", Toast.LENGTH_SHORT).show();
        }
    }


}
