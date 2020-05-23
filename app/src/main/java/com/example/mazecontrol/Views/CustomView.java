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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import com.example.mazecontrol.Views.Cell;
import com.example.mazecontrol.Views.CellGroup;
import com.remote.UDPConstant;

// 定义了多种创建类的初始化方法，应对不同的情况
public class CustomView extends View {

    private Paint mPaint = new Paint();

    private enum Direction{
        UP,DOWN,LEFT,RIGHT
    }

    private Cell[][] cells;
    private ArrayList<Cell> DoorPairCellA = new ArrayList<>();
    private ArrayList<Cell> DoorPairCellB = new ArrayList<>();
    private ArrayList<Cell> DoorPairCellC = new ArrayList<>();

    private Cell player,exit;
    private static final int COLS=MazeConstant.COLS, ROWS=MazeConstant.ROWS,
            GOLD_NUM=MazeConstant.GOLD_NUM,DOOR_NUM=MazeConstant.NUM_DOOR;
    private static final int NUM_PLAYER=MazeConstant.NUM_PLAYER;
    private int gold_num_record=GOLD_NUM;
    private float cellSize,hMargin,vMargin;
    private static final float WALL_THICKNESS=4;
    private Paint wallPaint,playerPaint,exitPaint,goldPaint,doorPaint,doorTouchPaint;


    private int player_id; // -1 admin, 0<id = player
    private int[][] player_group; // use stack for unlimited players
    private MazeConstant.role role=MazeConstant.role.PLAYER; //default at player

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

        doorPaint=new Paint();
        doorPaint.setColor(0xFFFF7F50);

        doorTouchPaint=new Paint();
        doorTouchPaint.setColor(0xFF7FC2D8);
    }
    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawColor(Color.LTGRAY);


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
                // indicat it can be touch
                if(this.role==MazeConstant.role.ADMIN){
                    // draw door shaft
                    if(cells[x][y].doorCell){
                        int cellWallSum = cells[x][y].doorWalls[0]+cells[x][y].doorWalls[1];
                        if(cellWallSum==(MazeConstant.TOPWALL+MazeConstant.LEFTWALL)){
                            canvas.drawCircle(cells[x][y].col*cellSize,cells[x][y].row*cellSize,margin,doorPaint);
                        }else if(cellWallSum==(MazeConstant.TOPWALL+MazeConstant.RIGHTWALL)){
                            canvas.drawCircle((cells[x][y].col+1)*cellSize,cells[x][y].row*cellSize,margin,doorPaint);
                        }else if(cellWallSum==(MazeConstant.BOTTOMWALL+MazeConstant.LEFTWALL)){
                            canvas.drawCircle(cells[x][y].col*cellSize,(cells[x][y].row+1)*cellSize,margin,doorPaint);
                        }else if(cellWallSum==(MazeConstant.BOTTOMWALL+MazeConstant.RIGHTWALL)){
                            canvas.drawCircle((cells[x][y].col+1)*cellSize,(cells[x][y].row+1)*cellSize,margin,doorPaint);
                        }else{
                            Log.d("ShowMazeActivity",String.format("can't draw door at %d %d", x,y));
                        }
                        canvas.drawRect(
                                cells[x][y].col*cellSize+margin,
                                cells[x][y].row*cellSize+margin,
                                (cells[x][y].col+1)*cellSize-margin,
                                (cells[x][y].row+1)*cellSize-margin,
                                doorTouchPaint
                        );
                    }
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
        if(player==exit && gold_num_record<=0) {
            gold_num_record=GOLD_NUM;
            createMaze();
            DoorPairCellA = new ArrayList<>();
            createDoors();
        }

    }
    private void reverseDoorWall(Cell cell,int i){
        switch (cell.doorWalls[i]){
            case MazeConstant.TOPWALL:
                cell.topWall=!cell.topWall;
                break;
            case MazeConstant.LEFTWALL:
                cell.leftWall=!cell.leftWall;
                break;
            case MazeConstant.RIGHTWALL:
                cell.rightWall=!cell.rightWall;
                break;
            case MazeConstant.BOTTOMWALL:
                cell.bottomWall=!cell.bottomWall;
                break;
            default:
                Log.d("ShowMazeActivity",String.format("can't move door"));
                break;
        }
    }
    private boolean moveDoor(int doorCellIndex){
        Cell doorcell=DoorPairCellA.get(doorCellIndex);
        Cell doorcellB=DoorPairCellB.get(doorCellIndex);
        Cell doorcellC=DoorPairCellC.get(doorCellIndex);

        reverseDoorWall(doorcell,0);
        reverseDoorWall(doorcell,1);
        reverseDoorWall(doorcellB,0);
        reverseDoorWall(doorcellC,0);
//        int cellWallSum = doorcell.doorWalls[0]+doorcell.doorWalls[1];
//
//        if(cellWallSum==(MazeConstant.TOPWALL+MazeConstant.LEFTWALL)){
//            doorcell.topWall=!doorcell.topWall;
//            doorcell.leftWall=!doorcell.leftWall;
//
//        }else if(cellWallSum==(MazeConstant.TOPWALL+MazeConstant.RIGHTWALL)){
//            doorcell.topWall=!doorcell.topWall;
//            doorcell.rightWall=!doorcell.rightWall;
//        }else if(cellWallSum==(MazeConstant.BOTTOMWALL+MazeConstant.LEFTWALL)){
//            doorcell.bottomWall=!doorcell.bottomWall;
//            doorcell.leftWall=!doorcell.leftWall;
//        }else if(cellWallSum==(MazeConstant.BOTTOMWALL+MazeConstant.RIGHTWALL)){
//            doorcell.bottomWall=!doorcell.bottomWall;
//            doorcell.rightWall=!doorcell.rightWall;
//        }else{
//
//        }
        invalidate();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN)
            return true;

        if(event.getAction()==MotionEvent.ACTION_UP){
            if (this.role==MazeConstant.role.ADMIN){
                // event position
                float x = event.getX();
                float y = event.getY();
                for(int i = 0;i < DoorPairCellA.size(); i ++){
                    Cell doorcell=cells[DoorPairCellA.get(i).col][DoorPairCellA.get(i).row];

                    // doorcell location
                    float CenterX = hMargin + (doorcell.col+0.5f)*cellSize;
                    float CenterY = vMargin + (doorcell.row+0.5f)*cellSize;
                    // could be negative
                    float dx = x - CenterX;
                    float dy = y - CenterY;

                    float absDx = Math.abs(dx);
                    float absDy = Math.abs(dy);

                    if (absDx < 0.5f*cellSize && absDy < 0.5f*cellSize){
                        Log.d("ShowMazeActivity",String.format("click at col %d row %d",doorcell.col,doorcell.row));
                        if (moveDoor(i))
                            break;
                    }
                }
            }
            return true;
        }

        // ACTION_MOVE can only be get after ACTION_DOWN
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            // event position
            float x = event.getX();
            float y = event.getY();

            // only player can move
            if (this.role==MazeConstant.role.PLAYER){
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
        }

        return super.onTouchEvent(event);
    }

    public void onClickReGeneration(){
        Log.d("ShowMazeActivity","reGenerate New Maze");
//        return String.format("Successfully click ClickReGeneration,\nPass in message:%s",Pass_in);
        createMaze();
        DoorPairCellA = new ArrayList<>();
        createDoors();
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


    // set the admin role
    // initialzie player group
    public void setAdminRole(){
        Log.d("ShowMazeActivity",String.format("1111"));
        this.role=MazeConstant.role.ADMIN;
        createDoors();
        Log.d("ShowMazeActivity",String.format("1111"));
    }

    // create movable door
    private void createDoors(){
//        DoorPairCellB
        int gRow,gCol;
        Cell selected;
        Log.d("ShowMazeActivity",String.format("2222"));
        while (DoorPairCellA.size() < DOOR_NUM){
            gRow = random.nextInt(ROWS);
            gCol = random.nextInt(COLS);
            // not at the edge
            if ((gRow<ROWS-1 && gRow> 0 ) && (gCol< COLS-1 && gCol> 0 )){
                selected = cells[gCol][gRow];
                if (!selected.placeGold && !DoorPairCellA.contains(selected) &&
                        !DoorPairCellB.contains(selected) && !DoorPairCellC.contains(selected)){
                    if(setPairDoorWall(selected)){
                        DoorPairCellA.add(selected);
                        selected.doorCell=true;
                        Log.d("ShowMazeActivity",String.format("generate door at col %d row %d",gCol,gRow));
                    }
                }
            }

        }
    }

    private boolean setPairDoorWall(Cell cellA){

        if (!cellA.topWall){
            int [] arr = {MazeConstant.LEFTWALL,MazeConstant.RIGHTWALL};
            int index=(int)(Math.random()*arr.length);
            int randWall = arr[index];
            if ( (randWall==MazeConstant.RIGHTWALL&&!cellA.rightWall) ||
                    (randWall==MazeConstant.LEFTWALL&&!cellA.leftWall))
                return false;
            else{
                cellA.doorWalls[0]=MazeConstant.TOPWALL;
                cellA.doorWalls[1]=randWall;
                if(randWall==MazeConstant.RIGHTWALL){
                    DoorPairCellB.add(cells[cellA.col+1][cellA.row]);
                    cells[cellA.col+1][cellA.row].doorWalls[0]=MazeConstant.LEFTWALL;
                }
                else{
                    DoorPairCellB.add(cells[cellA.col-1][cellA.row]);
                    cells[cellA.col-1][cellA.row].doorWalls[0]=MazeConstant.RIGHTWALL;
                }
                DoorPairCellC.add(cells[cellA.col][cellA.row-1]);
                cells[cellA.col][cellA.row-1].doorWalls[0]=MazeConstant.BOTTOMWALL;
            }
        }
        else if(!cellA.leftWall){
            int [] arr = {MazeConstant.TOPWALL,MazeConstant.BOTTOMWALL};
            int index=(int)(Math.random()*arr.length);
            int randWall = arr[index];
            if ( (randWall==MazeConstant.TOPWALL&&!cellA.topWall) ||
                    (randWall==MazeConstant.BOTTOMWALL&&!cellA.bottomWall))
                return false;
            else{
                cellA.doorWalls[0]=MazeConstant.LEFTWALL;
                cellA.doorWalls[1]=randWall;
                if(randWall==MazeConstant.TOPWALL) {
                    DoorPairCellB.add(cells[cellA.col][cellA.row - 1]);
                    cells[cellA.col][cellA.row - 1].doorWalls[0]=MazeConstant.BOTTOMWALL;
                }
                else{
                    DoorPairCellB.add(cells[cellA.col][cellA.row+1]);
                    cells[cellA.col][cellA.row+1].doorWalls[0]=MazeConstant.TOPWALL;
                }
                DoorPairCellC.add(cells[cellA.col-1][cellA.row]);
                cells[cellA.col-1][cellA.row].doorWalls[0]=MazeConstant.RIGHTWALL;
            }
        }
        else if(!cellA.rightWall){
            int [] arr = {MazeConstant.TOPWALL,MazeConstant.BOTTOMWALL};
            int index=(int)(Math.random()*arr.length);
            int randWall = arr[index];
            if ( (randWall==MazeConstant.TOPWALL&&!cellA.topWall) ||
                    (randWall==MazeConstant.BOTTOMWALL&&!cellA.bottomWall))
                return false;
            else {
                cellA.doorWalls[0] = MazeConstant.RIGHTWALL;
                cellA.doorWalls[1] = randWall;
                if(randWall==MazeConstant.TOPWALL) {
                    DoorPairCellB.add(cells[cellA.col][cellA.row - 1]);
                    cells[cellA.col][cellA.row - 1].doorWalls[0]=MazeConstant.BOTTOMWALL;
                }
                else{
                    DoorPairCellB.add(cells[cellA.col][cellA.row+1]);
                    cells[cellA.col][cellA.row+1].doorWalls[0]=MazeConstant.TOPWALL;
                }
                DoorPairCellC.add(cells[cellA.col+1][cellA.row]);
                cells[cellA.col+1][cellA.row].doorWalls[0]=MazeConstant.LEFTWALL;
            }
        }
        else if(!cellA.bottomWall){
            int [] arr = {MazeConstant.LEFTWALL,MazeConstant.RIGHTWALL};
            int index=(int)(Math.random()*arr.length);
            int randWall = arr[index];
            if ( (randWall==MazeConstant.RIGHTWALL&&!cellA.rightWall) ||
                    (randWall==MazeConstant.LEFTWALL&&!cellA.leftWall))
                return false;
            else {
                cellA.doorWalls[0] = MazeConstant.BOTTOMWALL;
                cellA.doorWalls[1] = randWall;
                if(randWall==MazeConstant.RIGHTWALL){
                    DoorPairCellB.add(cells[cellA.col+1][cellA.row]);
                    cells[cellA.col+1][cellA.row].doorWalls[0]=MazeConstant.LEFTWALL;
                }
                else{
                    DoorPairCellB.add(cells[cellA.col-1][cellA.row]);
                    cells[cellA.col-1][cellA.row].doorWalls[0]=MazeConstant.RIGHTWALL;
                }
                DoorPairCellC.add(cells[cellA.col][cellA.row+1]);
                cells[cellA.col][cellA.row+1].doorWalls[0]=MazeConstant.TOPWALL;
            }
        }
        // the cell can't be used for movable doors
        else{
            return false;
        }
        return true;
    }

    public void setAllByCellGroup(CellGroup cg){
        player = cg.player;
        cells = cg.cells;
    }
    public void setTopoByCellGroup(CellGroup cg){
        cells = cg.cells;
    }
    public void setLocaBySpotLocation(Spot_Location sp){
        Log.d("Remote", "update adimn view");
        player= cells[sp.getLocationCol()][sp.getLocationRow()];
    }


    public CellGroup getCells(){
        return new CellGroup(cells,player,COLS,ROWS);
    }
    public Spot_Location getSelfSpotLocation(String device_id) {return new Spot_Location(device_id,player.col,player.row);}
}
