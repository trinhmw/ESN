package com.navi.team.emptyseatnavigator.businessobject;

import android.content.res.Resources;
import android.util.Log;

/**
 * Created by Melissa on 2/10/2015.
 */
public class ReserveSeatsController {
    private int[] color;
    private Seat[][] formation;
    private static final String TAG = "ReserveSeatsController";

    public ReserveSeatsController(Seat[][] formation) {
        setColor(100,100,100);
        this.formation = formation;
    }

    public int reserveSeats(){
        Resources res;
        return 0;
    }

    public int[] getColor() {
        return color;
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

    public int getR() {
        return color[0];
    }

    public int getG() {
        return color[1];
    }

    public int getB() {
        return color[2];
    }

    public Seat[][] getFormation() {
        return formation;
    }

    public void setFormation(Seat[][] formation) {
        this.formation = formation;
    }
}

