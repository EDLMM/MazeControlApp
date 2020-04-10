package com.example.mazecontrol.Views;

import com.example.mazecontrol.Views.Cell;

public class CellGroup implements java.io.Serializable {
    public Cell[][] cells;
    public int COLS,ROWS;
    public CellGroup(Cell[][] current_cells, int n_col, int n_row){
        this.COLS=n_col;
        this.ROWS=n_row;
        cells = new Cell[COLS][ROWS];

        for (int c =0; c<COLS;c++){
            for (int r=0;r<ROWS;r++){
                cells[c][r] = new Cell(current_cells[c][r].col,current_cells[c][r].row);
            }
        }
    }
}
