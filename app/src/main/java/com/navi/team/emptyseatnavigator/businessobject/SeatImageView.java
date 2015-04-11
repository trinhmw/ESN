package com.navi.team.emptyseatnavigator.businessobject;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Melissa on 4/10/2015.
 */
public class SeatImageView extends ImageView{
    private int row;
    private int column;
    private boolean isAvailable;
    private int color;
    private Seat seat;

    public SeatImageView(Context context) {
        super(context);
    }

    public SeatImageView(Context context, Seat seat) {
        super(context);
        this.row = seat.getRow();
        this.column = seat.getCol();
        this.isAvailable = seat.isAvailable();
        this.seat = seat;
    }

    public Seat getNewSeat(){
        int[] empty = {0,0,0};
        Seat seat = new Seat(isAvailable, empty , row, column);
        return seat;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
