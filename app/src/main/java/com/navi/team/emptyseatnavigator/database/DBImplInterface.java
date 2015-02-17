package com.navi.team.emptyseatnavigator.database;

import com.navi.team.emptyseatnavigator.businessobject.Seat;

/**
 * Created by Melissa on 2/16/2015.
 * Required queries that must be implemented stated here
 */

public interface DBImplInterface {
    public boolean retrieveAvailableSeats();
    public boolean reserveSeats(Seat[] seatFormation);
}
