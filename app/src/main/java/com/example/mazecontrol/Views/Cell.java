package com.example.mazecontrol.Views;

import java.io.Serializable;


public class Cell implements Serializable {
    boolean
            topWall = true,
            leftWall = true,
            rightWall = true,
            bottomWall = true,
            visited = false, // only for creation
            placeGold = false,
            doorCell = false; // if this cell is for doors //every door can be control by clicking a cell

    // 0 1 2 3 -> topWall leftWall rightWall bottomWall
    public int[] doorWalls;

    int col,row;
    public Cell(int col, int row){
        this.col=col;
        this.row=row;
        doorWalls=new int[2];
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
        doorCell = cell.doorCell;
        doorWalls=new int[2];
        doorWalls[0]=cell.doorWalls[0];
        doorWalls[1]=cell.doorWalls[1];
    }
}
