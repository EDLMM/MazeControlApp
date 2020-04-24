package com.example.mazecontrol.Views;

import java.io.Serializable;

public class CellGroup implements Serializable {
    public String message="Message";
    //专门用来序列化传输
    public Cell[][] cells;
    public int COLS,ROWS;
    public Cell player;

    public CellGroup(Cell[][] current_cells, Cell current_player, int n_col, int n_row){
        this.COLS=n_col;
        this.ROWS=n_row;
        cells = new Cell[COLS][ROWS];

        for (int c =0; c<COLS;c++){
            for (int r=0;r<ROWS;r++){
//                cells[c][r] = new Cell(current_cells[c][r].col,current_cells[c][r].row);
                cells[c][r] = new Cell(current_cells[c][r]);
            }
        }
        this.player = current_player;
    }
}
