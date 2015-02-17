package com.navi.team.emptyseatnavigator.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by Melissa on 2/16/2015.
 * Called in RDBImpl to create a seat table for the database
 */
public class SeatTable{
    private static final String TABLE_NAME = "seat";
    private static final String ID = "id";
    private static final String ROW = "row";
    private static final String COLUMN = "column";
    private static final String COLOR_R = "R";
    private static final String COLOR_G = "G";
    private static final String COLOR_B = "B";
    private static final String SEAT_AVAILABLE = "isAvailable";



    public static void onCreate(SQLiteDatabase database) {
        SQLiteStatement statement = database.compileStatement("create table ? (? integer primary key autoincrement, ? integer, ? integer, ? integer, ? integer, ? integer, ? text");
        statement.bindString(1,TABLE_NAME);
        statement.bindString(2, ID);
        statement.bindString(3, ROW);
        statement.bindString(4, COLUMN);
        statement.bindString(5, COLOR_R);
        statement.bindString(6, COLOR_G);
        statement.bindString(7, COLOR_B);
        statement.bindString(8, SEAT_AVAILABLE);
        statement.execute();

    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        SQLiteStatement statement = database.compileStatement("drop table if exists ?");
        statement.bindString(1, TABLE_NAME);
        statement.execute();
        onCreate(database);

    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String getId() {
        return ID;
    }

    public static String getRow() {
        return ROW;
    }

    public static String getColumn() {
        return COLUMN;
    }

    public static String getColorR() {
        return COLOR_R;
    }

    public static String getColorG() {
        return COLOR_G;
    }

    public static String getColorB() {
        return COLOR_B;
    }

    public static String getSeatAvailable() {
        return SEAT_AVAILABLE;
    }
}
