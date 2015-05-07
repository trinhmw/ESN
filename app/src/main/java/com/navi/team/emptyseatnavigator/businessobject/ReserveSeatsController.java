package com.navi.team.emptyseatnavigator.businessobject;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.activities.SeatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Melissa on 2/10/2015.
 */
public class ReserveSeatsController{
    private static ReserveSeatsController instance;
    private int[] color;
    private int[] possibleColors = {R.color.color0, R.color.color1, R.color.color2, R.color.color3, R.color.color4};
    private int colorIndex;
    private Seat[] formation;
    private static final String TAG = "ReserveSeatsController";
    private Resources res;
    private CountDownTimer cdt;
    private long timeout = TimeUnit.SECONDS.toMillis(20);

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
     * Reserves the seats given an array of seats to be reserved.
     * @param formation
     * @return
     */
    public int[] reserveSeats(Seat[] formation, final SeatActivity activity){
        setColor(0,0,0);
        DBController dbc = DBController.getController();

        if(setFormation(formation)){
            for(Seat seat : formation){
                seat.setColor(hexToRGB(res.getColor(possibleColors[colorIndex])));
                seat.setReserved(true);
            }
//            If reservation successful, pass the next possible color
            if(dbc.reserveSeats(formation, activity)) {
                setColor(hexToRGB(res.getColor(possibleColors[colorIndex])));
                colorIndexAdjust();

                for(final Seat seat : formation) {
                    cdt = new CountDownTimer(timeout, TimeUnit.SECONDS.toMillis(1)) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            //commented out check due to it not working after first cdt
                            if(seat.isAvailable()) {
                                int[] availColor = {0, 255, 0};
                                Seat newSeat = new Seat(true, availColor, seat.getRow(), seat.getCol());
                                newSeat.setReserved(false);
                                DBController.getController().updateSeat(newSeat);
                                activity.sendMessage(newSeat);
                                activity.seatUpdateRefresh();
                            } else{
                                seat.setReserved(false);
                                activity.seatUpdateRefresh();
                            }
                        }
                    };
                    cdt.start();
                }

            }

        }
        return color;
    }


    /**
     * Rotates the color reservation colors
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

    public int[] getPossibleColors() {
        return possibleColors;
    }

    public int[] getCurrentPossibleColors() {
        return hexToRGB(res.getColor(possibleColors[colorIndex]));
    }

    public int getCurrentPossibleColors2() {
        return res.getColor(possibleColors[colorIndex]);
    }

    private void setPossibleColors(int[] possibleColors) {
        this.possibleColors = possibleColors;
    }
}

