package com.navi.team.emptyseatnavigator.database;

import android.content.Context;

import com.navi.team.emptyseatnavigator.businessobject.Seat;

/**
 * Created by Melissa on 2/16/2015.
 * Will be called by controllers to perform functions
 */
public class DatabaseManager {
    private DBImplInterface imp;

    public DatabaseManager(DBImplInterface imp, Context context) {
        this.imp = new RDBImpl(context);
    }

    public boolean retrieveAvailableSeats() {
        return imp.retrieveAvailableSeats();
    }

    public boolean reserveSeats(Seat[] seatFormation) {
        return imp.reserveSeats(seatFormation);
    }
}
