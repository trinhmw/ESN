package com.navi.team.emptyseatnavigator.Test.JUnit;

import com.navi.team.emptyseatnavigator.businessobject.InputController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;
import com.navi.team.emptyseatnavigator.businessobject.SeatingLogic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InputControllerTest {
    final int MAX_GROUP_SIZE = 4;
    String seatPreference;
    int groupSize;
    Seat[][] expected;
    Seat[][] actuals;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    /**
     * testValidateInputInvalidGroupSize - UIC1
     */
    public void testValidateInputInvalidGroupSize() throws Exception {
        groupSize = 0;
        seatPreference = "Front";
        actuals = InputController.validateInput(groupSize,seatPreference,MAX_GROUP_SIZE);
        expected = null;
        assertEquals(expected, actuals);
    }

    @Test
    /**
     * testValidateInputInvalidSeatPreference - UIC1
     */
    public void testValidateInputInvalidSeatPreference() throws Exception {
        groupSize = 1;
        seatPreference = "";
        actuals = InputController.validateInput(groupSize,seatPreference,MAX_GROUP_SIZE);
        expected = null;
        assertEquals(expected, actuals);
    }

//    @Test
    /**
     * testValidateInputValid - UIC2
     */
//    public void testValidateInputValid() throws Exception {
//        groupSize = 0;
//        seatPreference = "";
//        expected = false;
//        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
//        assertEquals(expected, actuals);
//    }


}