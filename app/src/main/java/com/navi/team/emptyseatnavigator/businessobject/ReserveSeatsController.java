package com.navi.team.emptyseatnavigator.businessobject;

import com.navi.team.emptyseatnavigator.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Melissa on 2/10/2015.
 */
public class ReserveSeatsController{
    private static ReserveSeatsController instance;
    private int[] color;
    private int[] possibleColors = {R.color.color0, R.color.color1, R.color.color2, R.color.color3};
    private int colorIndex;
    private Seat[] formation;
    private static final String TAG = "ReserveSeatsController";
    private Resources res;
    private DBController dbController;

    public static ReserveSeatsController getInstance(Context context){
        if(instance == null){
            instance = new ReserveSeatsController(context.getApplicationContext());
        }
        return instance;
    }

    public ReserveSeatsController(Context context){
        colorIndex = 0;
        res = context.getResources();
    }

    /**
     * reserveSeats - Reserves the seats given an array of seats to be reserved.
     * @param formation
     * @return
     */
    public int[] reserveSeats(Seat[] formation){
        boolean isValid = true;
        setColor(0,0,0);
        ArrayList<int[][]> seats;
        boolean reserved = false;

        if(setFormation(formation) == false){
            isValid = false;
        }

        if(isValid == true) {


//        Call isReserved= MapModule.reserveseat() or something like that here
//            If reserve successful then

            reserved = DBController.getController().reserveSeats(formation);

//            If reservation successful, pass the next possible color
            if(reserved) {
                setColor(hexToRGB(res.getColor(possibleColors[colorIndex])));
                colorIndexAdjust();
            }
        }
        return color;
    }


    /**
     * colorIndexAdjust - Rotates the color reservation colors
     */
    private void colorIndexAdjust(){
        if(colorIndex >= (possibleColors.length - 1)){
            colorIndex = 0;
        }
        else {
            colorIndex++;
        }
    }

    public int[] getColor() {
        return color;
    }

    private boolean setColor(int[] color) {
        boolean isSet = false;
        if (color[0] <= 255 && color[0] >= 0 && color[1] <= 255 && color[1] >= 0 && color[2] <= 255 && color[2] >= 0) {
            this.color = color;
            isSet = true;
        } else {
            Log.e(TAG, "RGB int value is not within the valid range of 0 to 255.");
        }
        return isSet;
    }

    public int[] hexToRGB(int hex){
        int r = Color.red(hex);
        int g = Color.green(hex);
        int b = Color.blue(hex);
        int[] color = {r,g,b};
        return color;
    }

    public String rgbToHex(int r, int g, int b){
        String hexColor = String.format( "#%02x%02x%02x", r, g, b );
        return hexColor;
    }

    private boolean setColor(int r, int g, int b) {
        boolean isSet = false;
        if (r <= 255 && r >= 0 && g <= 255 && g >= 0 && b <= 255 && b >= 0) {
            int[] color = {r, g, b};
            this.color = color;
            isSet = true;
        } else {
            Log.e(TAG, "RGB int value is not within the valid range of 0 to 255.");
        }

        return isSet;
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

    public Seat[] getFormation() {
        return formation;
    }

    public boolean setFormation(Seat[] formation) {
        boolean isSet = false;
        if(!(formation == null))
        {
            this.formation = formation;
            isSet = true;
        }
        return isSet;
    }
}

