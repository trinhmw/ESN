package com.navi.team.emptyseatnavigator.businessobject;

import android.util.Log;

import com.navi.team.emptyseatnavigator.activities.SeatActivity;

import java.io.Serializable;

/**
 * Created by Melissa on 2/10/2015.
 */
public class Seat implements Serializable {
    private Boolean isAvailable;
    private int[] color;
    private static final String TAG = "SeatObject";
    private int column;
    private int row;


    public Seat(Boolean isAvailable, int[] color) {
        this.isAvailable = isAvailable;
        this.color = color;

    }
    public Seat(Boolean isAvailable) {
        setColor(0,0,0);
        this.isAvailable = isAvailable;
    }
    public Seat() {
        setColor(0,0,0);
        this.isAvailable = false;
    }

    public Seat(Boolean isAvailable, int[] color, int row, int column){
        this.isAvailable = isAvailable;
        this.color = color;
        this.row = row;
        this.column = column;
    }

    public Seat(int row, int column){
        this.isAvailable = false;
        setColor(0,0,0);
        this.row = row;
        this.column = column;
    }

    public Seat(int row, int column, Boolean isAvailable){
        this.isAvailable = isAvailable;
        setColor(0,0,0);
        this.row = row;
        this.column = column;
    }

    public Boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public int[] getColor() {
        return color;
    }

    public int getR() {
        return color[0];
    }

    public int getG() {
        return color[1];
    }

    public int getB() {
        return color[2];
    }

    public void setColor(int[] color) {
        this.color = color;
    }

    public boolean setColor(int r, int g, int b) {
        boolean isValid = false;
        if (r <= 255 && r >= 0 && g <= 255 && g >= 0 && b <= 255 && b >= 0) {
            int[] color = {r, g, b};
            this.color = color;
            isValid = true;
        } else {
//            Log.e(TAG, "RGB int value is not within the valid range of 0 to 255.");
        }
        return isValid;
    }

    public int getCol() {
        return column;
    }

    public void setCol(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
