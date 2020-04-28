package com.example.mazecontrol.Views;

import java.io.Serializable;


// for player updating location
// can be adapted to for further use
public class Spot_Location implements Serializable {
    private String p_id;
    private int locationCol,locationRow;

    Spot_Location(String p_id){
        this.p_id=p_id;
        locationCol = 0;
        locationRow= 0;
    }

    public void setLocationCol(int locationCol) {
        this.locationCol = locationCol;
    }

    public void setLocationRow(int locationRow) {
        this.locationRow = locationRow;
    }

    public int getLocationCol() {
        return locationCol;
    }

    public int getLocationRow() {
        return locationRow;
    }

    public String getP_id() {
        return p_id;
    }
}
