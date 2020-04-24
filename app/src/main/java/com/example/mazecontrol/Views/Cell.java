package com.example.mazecontrol.Views;

import java.io.Serializable;

public class Cell implements Serializable {
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

    public Cell(Cell cell) {
        col=cell.col;
        row=cell.row;
        topWall=cell.topWall;
        leftWall = cell.leftWall;
        rightWall = cell.rightWall;
        bottomWall = cell.bottomWall;
        visited = cell.visited; // only for creation
        placeGold = cell.placeGold;
    }
}
