package com.example.mazecontrol.Views;

import android.content.Context;
import android.content.ReceiverCallNotAllowedException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;


// 定义了多种创建类的初始化方法，应对不同的情况
public class CustomView extends View {

    private Paint mPaint = new Paint();

    private enum Direction{
        UP,DOWN,LEFT,RIGHT
    }

    private Cell[][] cells;
    private Cell player,exit;
    private static final int COLS=7, ROWS=10, GOLD_NUM=5;
    private int gold_num_record=GOLD_NUM;
    private float cellSize,hMargin,vMargin;
    private static final float WALL_THICKNESS=4;
    private Paint wallPaint,playerPaint,exitPaint,goldPaint;

    private Random random;

    public CustomView(Context context) {
        super(context);

        init(null);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(attrs);
    }

    protected void init(@Nullable AttributeSet set){

        random = new Random();
        initPaint();
        createMaze();
    }
    private void initPaint() {
//        mPaint.setColor(Color.BLACK);
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setStrokeWidth(10f);

        wallPaint=new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint=new Paint();
        playerPaint.setColor(Color.RED);

        exitPaint=new Paint();
        exitPaint.setColor(Color.BLUE);

        goldPaint=new Paint();
        goldPaint.setColor(Color.YELLOW);

    }
    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawColor(Color.LTGRAY);
//        canvas.drawPoint(5,5,mPaint);
//        canvas.drawPoint(200,200,mPaint);
//        canvas.drawPoints(new float[]{          //绘制一组点，坐标位置由float数组指定
//                500,500,
//                500,600,
//                500,700
//        },mPaint);

        int width=getWidth();
        int height=getHeight();

        if(width/height<COLS/ROWS){
            cellSize=width/(COLS+1);
        }else{
            cellSize=height/(ROWS+1);
        }
        hMargin = (width-COLS*cellSize)/2;
        vMargin = (height-ROWS*cellSize)/2;
        float margin = cellSize/10; //rectangle draw margin
        canvas.translate(hMargin,vMargin); // 修改坐标系原点

        for (int x=0; x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                if(cells[x][y].topWall){
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            y*cellSize,
                            wallPaint
                    );
                }
                if(cells[x][y].leftWall){
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            x*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                }
                if(cells[x][y].bottomWall){
                    canvas.drawLine(
                            x*cellSize,
                            (y+1)*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                }
                if(cells[x][y].rightWall){
                    canvas.drawLine(
                            (x+1)*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                }

                // draw golds
                if(cells[x][y].placeGold){
                    canvas.drawRect(
                            cells[x][y].col*cellSize+margin,
                            cells[x][y].row*cellSize+margin,
                            (cells[x][y].col+1)*cellSize-margin,
                            (cells[x][y].row+1)*cellSize-margin,
                            goldPaint
                    );
                }
            }
        }


        canvas.drawRect(
            player.col*cellSize+margin,
            player.row*cellSize+margin,
            (player.col+1)*cellSize-margin,
            (player.row+1)*cellSize-margin,
            playerPaint
        );

        canvas.drawRect(
                exit.col*cellSize+margin,
                exit.row*cellSize+margin,
                (exit.col+1)*cellSize-margin,
                (exit.row+1)*cellSize-margin,
                exitPaint
        );

    }


    // Player Movement
    // change the player cell
    private void movePlayer(Direction direction){
        switch (direction){
            case UP:
                if(!player.topWall)
                    player = cells[player.col][player.row-1];
                break;
            case DOWN:
                if(!player.bottomWall)
                    player = cells[player.col][player.row+1];
                break;
            case LEFT:
                if(!player.leftWall)
                    player = cells[player.col-1][player.row];
                break;
            case RIGHT:
                if(!player.rightWall)
                    player = cells[player.col+1][player.row];
                break;
        }
        checkEatGold();
        checkSuccess();
        invalidate();
    }

    private void checkEatGold(){
        if (player.placeGold==true){
            player.placeGold=false;
            gold_num_record--;
        }
    }

    private void checkSuccess(){
        if(player==exit && gold_num_record<=0)
            createMaze();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        if(event.getAction()==MotionEvent.ACTION_DOWN)
           return true;

        // ACTION_MOVE can only be get after ACTION_DOWN
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            // event position
            float x = event.getX();
            float y = event.getY();
            // player location
            float playerCenterX = hMargin + (player.col+0.5f)*cellSize;
            float playerCenterY = vMargin + (player.row+0.5f)*cellSize;

            // could be negative
            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);

            if (absDx > cellSize || absDy > cellSize){
                //move in x-direction
                if (absDx > absDy){
                    // move to right
                    if ( dx > 0){
                        movePlayer(Direction.RIGHT);
                    }
                    // move to left
                    else{
                        movePlayer(Direction.LEFT);
                    }
                }
                //move in y-direction
                else{
                    // move down
                    if(dy > 0){
                        movePlayer(Direction.DOWN);
                    }
                    // move up
                    else{
                        movePlayer(Direction.UP);
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public void onClickReGeneration(){
        Log.d("ShowMazeActivity","reGenerate New Maze");
//        return String.format("Successfully click ClickReGeneration,\nPass in message:%s",Pass_in);
        createMaze();
        invalidate();//refresh
    }

    // Maze definition
    // recursive back tracker for randomly maze creation
    private void createMaze(){
        Stack<Cell> stack = new Stack<>();
        Cell current,next;


        cells = new Cell[COLS][ROWS];

        for (int x=0; x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                cells[x][y]= new Cell(x , y);

            }
        }

        // settings
        player = cells[0][0];
        exit=cells[COLS-1][ROWS-1];

        current=cells[0][0]; //set to entry
        current.visited=true;
        do {
            next = getRandomUnvisitedNeighbour(current); //randomly get a neighbour
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else
                current = stack.pop();
        }while(!stack.empty());

        // place golds
        setGoldPlace();
    }

    private Cell getRandomUnvisitedNeighbour(Cell cell){
        ArrayList<Cell> neighbours = new ArrayList<>();

        // left neighbour
        if(cell.col > 0)
            if (! cells[cell.col -1 ][cell.row].visited)
                neighbours.add(cells[cell.col -1 ][cell.row]);
        // right neighbour
        if(cell.col < COLS-1)
            if (! cells[cell.col +1 ][cell.row].visited)
                neighbours.add(cells[cell.col +1 ][cell.row]);
        // top neighbour
        if(cell.row > 0)
            if (! cells[cell.col ][cell.row - 1].visited)
                neighbours.add(cells[cell.col][cell.row-1]);
        // bottom neighbour
        if(cell.row < ROWS-1)
            if (! cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col][cell.row+1]);

        if (neighbours.size()>0){
            // get one index of the neighbour list
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }else{
            return null;
        }
    }

    private void setGoldPlace(){
        ArrayList<Cell> golds = new ArrayList<>();

        int gRow,gCol;
        Cell selected;
        while (golds.size() < GOLD_NUM){
            gRow = random.nextInt(ROWS);
            gCol = random.nextInt(COLS);
            Log.d("ShowMazeActivity",String.format("3 generate gold at%d %d",gRow,gCol));
            selected = cells[gCol][gRow];
            if(selected!=cells[0][0]&&selected!=cells[COLS-1][ROWS-1]){
                if (!golds.contains(selected)){
                    golds.add(selected);
                    selected.placeGold=true;
                }
            }
        }
    }

    // only use in creation
    private void removeWall(Cell current, Cell next){
        //if current bellow next
        if(current.col == next.col && current.row ==next.row+1){
            current.topWall=false;
            next.bottomWall=false;
        }
        //if current above next
        if(current.col == next.col && current.row ==next.row-1){
            current.bottomWall=false;
            next.topWall=false;
        }
        //if current to the right of next
        if(current.col == next.col+1 && current.row ==next.row){
            current.leftWall=false;
            next.rightWall=false;
        }
        //if current to the left of next
        if(current.col == next.col-1 && current.row ==next.row){
            current.rightWall=false;
            next.leftWall=false;
        }
    }

    private class Cell{
        boolean
                topWall = true,
                leftWall = true,
                rightWall = true,
                bottomWall = true,
                visited = false, // only for creation
                placeGold = false;

        int col,row;
        public Cell(int col, int row){
            this.col=col;
            this.row=row;
        }
    }
}
