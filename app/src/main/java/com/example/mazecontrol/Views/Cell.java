package com.example.mazecontrol.Views;

public class Cell {
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
