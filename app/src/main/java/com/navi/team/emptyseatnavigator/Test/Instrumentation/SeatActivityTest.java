package com.navi.team.emptyseatnavigator.Test.Instrumentation;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.method.Touch;
import android.view.Gravity;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.activities.SeatActivity;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

public class SeatActivityTest extends ActivityInstrumentationTestCase2<SeatActivity>{
//    final SeatActivity mSeatActivity = getActivity();

//    private NumberPicker pickerGroupSize;
//    private RadioGroup seatPreferences;
//    private RadioButton preference;
//    private Button reserveButton;
//    private int checkedPreference;
//    private RadioButton frontPreference;

    public SeatActivityTest() {
        super(SeatActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        setActivityInitialTouchMode(true);
//        mSeatActivity = getActivity();

//        pickerGroupSize = (NumberPicker) mSeatActivity.findViewById(R.id.pickerGroupSize);
//        seatPreferences = (RadioGroup) mSeatActivity.findViewById(R.id.seatPreferences);
//        checkedPreference = seatPreferences.getCheckedRadioButtonId();
//        preference = (RadioButton) mSeatActivity.findViewById(checkedPreference);
//        frontPreference = (RadioButton) mSeatActivity.findViewById(R.id.prefFront);
//        reserveButton = (Button) mSeatActivity.findViewById(R.id.buttonReserve);

    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    /**
     * testGroupSize - USV1
     */
    public void testGroupSize() throws Exception {
        final SeatActivity mSeatActivity = getActivity();
        NumberPicker pickerGroupSize = (NumberPicker) mSeatActivity.findViewById(R.id.pickerGroupSize);
        int expected = pickerGroupSize.getValue() + 1;
        int actual;
        TouchUtils.dragViewToY(this,pickerGroupSize, Gravity.TOP,1);
        actual = pickerGroupSize.getValue();
        assertEquals(expected, actual);
    }

    @SmallTest
    /**
     * testSeatPreference - USV2
     */
    public void testSeatPreference() throws Exception {
        final SeatActivity mSeatActivity = getActivity();
        RadioGroup seatPreferences = (RadioGroup) mSeatActivity.findViewById(R.id.seatPreferences);
        int checkedPreference = seatPreferences.getCheckedRadioButtonId();
        RadioButton preference;
        RadioButton frontPreference = (RadioButton) mSeatActivity.findViewById(R.id.prefFront);

        CharSequence expected = frontPreference.getText();
        CharSequence actual;


        TouchUtils.clickView(this, frontPreference);
        checkedPreference = seatPreferences.getCheckedRadioButtonId();
        preference = (RadioButton) mSeatActivity.findViewById(checkedPreference);
        actual = preference.getText();
        assertEquals(expected,actual);
    }

//    @SmallTest
//    /**
//     * testSubmitButton - USV3
//     */
//    public void testSubmitButton() throws Exception {
//        boolean expected = false;
//        boolean actual = false;
//        assertEquals(expected,actual);
//    }
//
//    @SmallTest
//    /**
//     * testDisplaySeatFormationSelection - USV4
//     */
//    public void testDisplaySeatFormationSelection() throws Exception {
//        boolean expected = false;
//        boolean actual = false;
//        assertEquals(expected,actual);
//    }
//
//    @SmallTest
//    /**
//     * testMakeReservationButtonUnsuccessful - USV5
//     */
//    public void testMakeReservationButtonUnsuccessful() throws Exception {
//        boolean expected = false;
//        boolean actual = false;
//        assertEquals(expected,actual);
//    }
//
//    @SmallTest
//    /**
//     * testMakeReservationButtonSuccessful - USV5
//     */
//    public void testMakeReservationButtonSuccessful() throws Exception {
//        boolean expected = false;
//        boolean actual = false;
//        assertEquals(expected,actual);
//    }
//
//    @SmallTest
//    /**
//     * testDisplayAvailableSeats - USV6
//     */
//    public void testDisplayAvailableSeats() throws Exception {
//        boolean expected = false;
//        boolean actual = false;
//        assertEquals(expected,actual);
//    }
//
//    @SmallTest
//    /**
//     * testDisplaySeatFormation - USV7
//     */
//    public void testDisplaySeatFormation() throws Exception {
//        boolean expected = false;
//        boolean actual = false;
//        assertEquals(expected,actual);
//    }

}