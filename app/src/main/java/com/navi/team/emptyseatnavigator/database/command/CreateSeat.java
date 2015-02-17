package com.navi.team.emptyseatnavigator.database.command;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;

import com.navi.team.emptyseatnavigator.database.table.SeatTable;

import java.sql.SQLException;

/**
 * Created by Melissa on 2/16/2015.
 */
public class CreateSeat extends SQLCmd{
    private int row;
    private int column;
    private int r;
    private int g;
    private int b;
    private String isAvailable;
    private String[] tableColumns = {SeatTable.getId(), SeatTable.getRow(), SeatTable.getColumn(), SeatTable.getColorR(), SeatTable.getColorG(), SeatTable.getColorB(), SeatTable.getSeatAvailable()};


    public CreateSeat(int row, int column, int r, int g, int b, String isAvailable) {
        this.row = row;
        this.column = column;
        this.r = r;
        this.g = g;
        this.b = b;
        this.isAvailable = isAvailable;
    }

    public CreateSeat(int row, int column) {
        this.row = row;
        this.column = column;
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.isAvailable = "y";
    }

    @Override
    public void queryDB() throws SQLException {
        ContentValues values = new ContentValues();
        values.put(SeatTable.getRow(), row);
        values.put(SeatTable.getColumn(), column);
        values.put(SeatTable.getColorR(),r);
        values.put(SeatTable.getColorG(),g);
        values.put(SeatTable.getColorB(),b);
        values.put(SeatTable.getSeatAvailable(),isAvailable);
        long result = database.insert(SeatTable.getTableName(), null, values); //result is the rowID of the newly inserted row or -1 if error occurred
//        String command = "";
//        SQLiteStatement statement = database.compileStatement(command);
    }

    @Override
    public void processResult() {

    }
}
