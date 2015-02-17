package com.navi.team.emptyseatnavigator.businessobject;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by Melissa on 2/10/2015.
 */
public class Seat implements Serializable {
    private String isAvailable;
    private int[] color;
    private static final String TAG = "SeatObject";


    public Seat(String isAvailable, int[] color) {
        this.isAvailable = isAvailable;
        this.color = color;
    }
    public Seat(String isAvailable) {
        int[] color = {0,0,0};
        this.isAvailable = isAvailable;
    }
    public Seat() {
        int[] color = {0,0,0};
        this.isAvailable = "n";
        this.color = color;
    }

    public String isAvailable() {
        return isAvailable;
    }

    public void setAvailable(String isAvailable) {
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

    public void setColor(int r, int g, int b) {
        if (r <= 255 && r >= 0 && g <= 255 && g >= 0 && b <= 255 && b >= 0) {
            int[] color = {r, g, b};
            this.color = color;
        } else {
            Log.e(TAG, "RGB int value is not within the valid range of 0 to 255.");
        }
    }
}
