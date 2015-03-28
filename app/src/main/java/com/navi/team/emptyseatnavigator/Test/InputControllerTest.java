package com.navi.team.emptyseatnavigator.Test;

import com.navi.team.emptyseatnavigator.businessobject.InputController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InputControllerTest {
    final int MAX_GROUP_SIZE = 4;
    String seatPreference;
    int groupSize;
    boolean expected;
    boolean actuals;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testValidateInput1() throws Exception {
        groupSize = 0;
        seatPreference = "";
        expected = false;
        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
        assertEquals(expected, actuals);
    }

    public void testValidateInput2() throws Exception {
        groupSize = 1;
        seatPreference = "";
        expected = false;
        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
        assertEquals(expected, actuals);
    }

    public void testValidateInput3() throws Exception {
        groupSize = 1;
        seatPreference = "Front";
        expected = true;
        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
        assertEquals(expected, actuals);
    }

    public void testValidateInput4() throws Exception {
        groupSize = 2;
        seatPreference = "Middle";
        expected = true;
        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
        assertEquals(expected, actuals);
    }

    public void testValidateInput5() throws Exception {
        groupSize = 3;
        seatPreference = "Back";
        expected = true;
        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
        assertEquals(expected, actuals);
    }

    public void testValidateInput6() throws Exception {
        groupSize = 4;
        seatPreference = "Back";
        expected = true;
        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
        assertEquals(expected, actuals);
    }

    public void testValidateInput7() throws Exception {
        groupSize = 5;
        seatPreference = "Back";
        expected = false;
        actuals = InputController.validateInput(groupSize, seatPreference, MAX_GROUP_SIZE);
        assertEquals(expected,actuals);

    }
}