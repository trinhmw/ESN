package com.navi.team.emptyseatnavigator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.navi.team.emptyseatnavigator.businessobject.Seat;
import com.navi.team.emptyseatnavigator.database.table.SeatTable;

/**
 * Created by Melissa on 2/16/2015.
 * Creates and upgrades database and tables
 */
public class RDBImpl extends SQLiteOpenHelper implements DBImplInterface {
    private static RDBImpl instance;
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    public static RDBImpl getInstance(Context context) {
        if(instance == null) {
            instance = new RDBImpl(context.getApplicationContext());
        }
        return instance;
    }

    protected RDBImpl(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * onCreate - Create tables for the database
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        SeatTable.onCreate(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SeatTable.onUpgrade(db,oldVersion,newVersion);
    }

    @Override
    public boolean retrieveAvailableSeats() {
        return false;
    }

    @Override
    public boolean reserveSeats(Seat[] seatFormation) {
        return false;
    }

}
