package com.navi.team.emptyseatnavigator.Test.Instrumentation;

import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.navi.team.emptyseatnavigator.activities.SeatActivity;
import com.navi.team.emptyseatnavigator.businessobject.ReserveSeatsController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReserveSeatsControllerTest extends ActivityInstrumentationTestCase2<SeatActivity>{
    private SeatActivity mSeatActivity;

    public ReserveSeatsControllerTest() {
        super(SeatActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        mSeatActivity = getActivity();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }



    @SmallTest
    /**
     * testSetFormationEmpty - URSC1
     */
    public void testSetFormationEmpty(){
        Seat[] empty = null;
        ReserveSeatsController rsc = ReserveSeatsController.getInstance(mSeatActivity.getApplicationContext());
        boolean expected = false;
        boolean actual = rsc.setFormation(empty);
        assertEquals(expected,actual);
    }

    @SmallTest
    /**
     * testSetFormationValid - URSC1
     */
    public void testSetFormationValid(){
        Seat[] valid = new Seat[1];
        valid[0] = new Seat(true);
        ReserveSeatsController rsc = ReserveSeatsController.getInstance(mSeatActivity.getApplicationContext());
        boolean expected = true;
        boolean actual = rsc.setFormation(valid);
        assertEquals(expected,actual);
    }

    @SmallTest
    /**
     * testReserveSeatsEmptySeatFormation - URSC2
     */
    public void testReserveSeatsEmptySeatFormation() throws Exception {
        ReserveSeatsController rsc = ReserveSeatsController.getInstance(mSeatActivity.getApplicationContext());
        Seat[] formation = null;
        int[] expectedColor= {0,0,0};
        int[] actualColor = rsc.reserveSeats(formation,mSeatActivity);
        boolean expected = true;
        boolean actual = false;
        if((expectedColor[0] == actualColor[0])&&
                (expectedColor[1] == actualColor[1])&&
                (expectedColor[2] == actualColor[2])){
            actual = true;
        }
        assertEquals(expected,actual);
    }

//    @Test
    /**
     * testReserveSeatsValidSeatFormation - URSC2
     */
//    public void testReserveSeatsValidSeatFormation() throws Exception {
//        ReserveSeatsController rsc = ReserveSeatsController.getInstance(mSeatActivity.getApplicationContext());
//        Seat[] formation = null;
//        int[] expected = {0,0,0};
//        int[] actual = rsc.reserveSeats(formation);
//    }
}