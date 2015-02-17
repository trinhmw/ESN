package com.navi.team.emptyseatnavigator.database.command;

import android.database.sqlite.SQLiteDatabase;

import com.navi.team.emptyseatnavigator.database.RDBImpl;

import java.sql.SQLException;

/**
 * Created by Melissa on 2/16/2015.
 */
public abstract class SQLCmd {
    protected SQLiteDatabase database;
    private RDBImpl rdb;

    /**
     * open - Open database connection
     * @throws SQLException
     */
    public void open() throws SQLException{
        database = rdb.getWritableDatabase();
    }

    /**
     * close - Close database
     */
    public void close(){
        rdb.close();
    }

    /**
     * execute - Open database, make query, and close the database.
     */
    public void execute(){
        try{
            open();
            queryDB();
            processResult();
            close();
        }
        catch (Exception e){
            close();
        }
    }

    public abstract void queryDB() throws SQLException;

    public abstract void processResult();
}
