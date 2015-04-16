package com.navi.team.emptyseatnavigator.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.businessobject.Constants;
import com.navi.team.emptyseatnavigator.businessobject.DBController;
import com.navi.team.emptyseatnavigator.businessobject.InputController;
import com.navi.team.emptyseatnavigator.businessobject.ReserveSeatsController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;
import com.navi.team.emptyseatnavigator.businessobject.SeatImageView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class SeatActivity extends Activity implements Constants{

    private final static String TAG = SeatActivity.class.getSimpleName();

    private int[] reserveColor;
    private int[][] availableSeats = new int[MAX_ROW][MAX_COLUMN];
    private Seat[][] seatFormation;
    private Seat[] selectedFormation;
    private ArrayList<Seat> touchSelection = null;
    private int selectedFormationIndex = 0;
    private LinearLayout[] tempLinLayout;
    private final String ERROR_TITLE = "Hey Listen!";
    private SeatActivity seatActivity;

    // USB Communications Variables & Constants
    private PendingIntent mPermissionIntent;
    private final static String ACTION_USB_PERMISSION = "com.navi.team.USB_PERMISSION";
    private boolean mPermissionRequestPending;
    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;
    private static final byte CMD_LED = 0x0;
    private static final byte CMD_SWITCH = 0x1;

    private final int height = 70;
    private final int width = 70;
    private final int seatMargin = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setIcon(R.drawable.ic_launcher);
//        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        seatActivity = this;
        // Group size picker
        final NumberPicker pickerGroupSize = (NumberPicker) findViewById(R.id.pickerGroupSize);
        pickerGroupSize.setMaxValue(MAX_GROUP_SIZE);
        pickerGroupSize.setMinValue(1);
        pickerGroupSize.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerGroupSize.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Button reserveButton = (Button) findViewById(R.id.buttonReserve);
                if(seatFormation != null){
                    if(newVal != seatFormation[selectedFormationIndex].length){
                        reserveButton.setEnabled(false);
                    } else {
                        reserveButton.setEnabled(true);
                    }
                }
                else if(touchSelection != null){
                    if(newVal != touchSelection.size()){
                        reserveButton.setEnabled(false);
                    } else {
                        reserveButton.setEnabled(true);
                    }
                } else {
                    reserveButton.setEnabled(false);
                }
            }
        });


        // Seat preferences radio buttons (Default None)
        final RadioGroup seatPreferences = (RadioGroup) findViewById(R.id.seatPreferences);
        seatPreferences.check(R.id.prefNone);

        // Submit button
        final Button submitButton = (Button) findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedPreference = seatPreferences.getCheckedRadioButtonId();
                RadioButton preference = (RadioButton) findViewById(checkedPreference);
                if (preference.getText() != null) {
                    calculateNumberOfAvailableSeats();
                    if(pickerGroupSize.getValue() > calculateNumberOfAvailableSeats()){
                        errorRefreshDialog("There are not enough seats available for your group.");
                    }
                    else {
                        seatFormation = null;
                        touchSelection = null;
                        seatFormation = InputController.validateInput(pickerGroupSize.getValue(), preference.getText().toString(), MAX_GROUP_SIZE);
                        if (seatFormation != null) {
                            selectedFormationIndex = 0;
                            displayFormation(seatFormation, selectedFormationIndex);
                            Button rightButton = (Button) findViewById(R.id.buttonRight);
                            Button leftButton = (Button) findViewById(R.id.buttonLeft);
                            Button reserveButton = (Button) findViewById(R.id.buttonReserve);
                            if(seatFormation.length>1) {
                                rightButton.setEnabled(true);
                                leftButton.setEnabled(true);
                            }
                            reserveButton.setEnabled(true);
                        } else {
                            errorDialog("No seat formations available.");
                        }
                    }
                }
            }
        });

//        // Lucky button
//        final Button luckyButton = (Button) findViewById(R.id.buttonLucky);
//        luckyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int checkedPreference = seatPreferences.getCheckedRadioButtonId();
//                RadioButton preference = (RadioButton) findViewById(checkedPreference);
//                if (preference.getText() != null) {
//                    calculateNumberOfAvailableSeats();
//                    if(pickerGroupSize.getValue() > calculateNumberOfAvailableSeats()){
//                        errorRefreshDialog("There are not enough seats available for your group.");
//                    }
//                    else {
//                        seatFormation = null;
//                        touchSelection = null;
//                        seatFormation = randomSeatFormation(pickerGroupSize.getValue());
//                        if (seatFormation != null) {
//                            selectedFormationIndex = 0;
//                            displayFormation(seatFormation, selectedFormationIndex);
//                            Button rightButton = (Button) findViewById(R.id.buttonRight);
//                            Button leftButton = (Button) findViewById(R.id.buttonLeft);
//                            Button reserveButton = (Button) findViewById(R.id.buttonReserve);
//                            if(seatFormation.length>1) {
//                                rightButton.setEnabled(true);
//                                leftButton.setEnabled(true);
//                            }
//                            reserveButton.setEnabled(true);
//                        } else {
//                            errorDialog("No seat formations available.");
//                        }
//                    }
//                }
//            }
//        });


        // Rotate Formation Left Button
        final Button leftButton = (Button) findViewById(R.id.buttonLeft);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success;
                if(seatFormation != null) {
                    success = formationIndexLeft();
                    if (success) {
                        displayFormation(seatFormation, selectedFormationIndex);
                        touchSelection = null;
                        Button reserveButton = (Button) findViewById(R.id.buttonReserve);
                        reserveButton.setEnabled(true);
                    }
                }
                else{
                    errorDialog("Please submit your group size and seat preferences first.");
                }
            }
        });
        leftButton.setEnabled(false);

        // Rotate Formation Right Button
        final Button rightButton = (Button) findViewById(R.id.buttonRight);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success;
                if(seatFormation != null) {
                    success = formationIndexRight();
                    if (success) {
                        displayFormation(seatFormation, selectedFormationIndex);
                        touchSelection = null;
                        Button reserveButton = (Button) findViewById(R.id.buttonReserve);
                        reserveButton.setEnabled(true);
                    }
                }
                else{
                    errorDialog("Please submit your group size and seat preferences first.");
                }
            }
        });
        rightButton.setEnabled(false);

        // Make Reservation Button
        final Button reserveButton = (Button) findViewById(R.id.buttonReserve);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReserveSeatsController rsc = ReserveSeatsController.getInstance(getApplicationContext());
                if ((seatFormation != null) || (touchSelection != null)) {
                    if(touchSelection != null){
                        selectedFormation = touchSelection.toArray(new Seat[touchSelection.size()]);
                    }
                    else if(seatFormation != null) {
                        selectedFormation = seatFormation[selectedFormationIndex];
                        selectedFormation = stripUnavailableFromFormation(seatFormation[selectedFormationIndex]);
                    }
                    if(pickerGroupSize.getValue() == selectedFormation.length) {
                        reserveColor = rsc.reserveSeats(selectedFormation, seatActivity);

                        if (!(reserveColor[0] == 0 && reserveColor[1] == 0 && reserveColor[2] == 0)) {
                            //Show reservation seat color with a confirmation button
                            //Clear the display to available seats again
                            reservedDialog(reserveColor[0], reserveColor[1], reserveColor[2]);
                        } else {
                            //Tell the user that their seats could not be reserved and refresh display back to available seats
                            errorRefreshDialog("Your seats have been taken, please try again.");
                        }
                    } else{
                        errorDialog("Please select the same amount of seats as your group size.");
                    }
                } else {
                    errorDialog("Please select a formation or select a seat.");
                }
            }
        });
        reserveButton.setEnabled(false);

        availableSeats = DBController.getController().getAvailableSeatsInt();

        tempLinLayout = displaySeats(availableSeats);

        // USB Communications Setup
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mInputStream != null && mOutputStream != null) {
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeAccessory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    /**
     * Determine when Arduino is attached and if permission is granted by user
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory " + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }
        }
    };

    private void openAccessory(UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Thread thread = new Thread(null, receiveMessage, TAG);
            thread.start();
            Log.d(TAG, "accessory opened");
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }

    private void closeAccessory() {
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    public void sendMessage(Seat seat) {
        byte buffer[] = new byte[6];

        buffer[0] = CMD_LED;
        buffer[1] = (byte) seat.getRow();
        buffer[2] = (byte) seat.getCol();
        buffer[3] = (byte) seat.getR();
        buffer[4] = (byte) seat.getG();
        buffer[5] = (byte) seat.getB();

        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }

    Runnable receiveMessage = new Runnable() {
        @Override
        public void run() {
            int ret = 0;
            byte[] buffer = new byte[255];

            while (ret >= 0) {
                try {
                    ret = mInputStream.read(buffer);
                } catch (IOException e) {
                    break;
                }

                switch (buffer[0]) {
                    case CMD_SWITCH:
                        int row = buffer[1];
                        int col = buffer[2];
                        Boolean isAvailable;
                        if (buffer[3] == 0) {
                            isAvailable = false;
                        } else {
                            isAvailable = true;
                        }
                        final Seat cmdSeat = new Seat(row, col, isAvailable);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DBController.getController().updateSeat(cmdSeat);
                                seatUpdateRefresh();
                            }
                        });

                        break;
                    default:
                        Log.d(TAG, "unknown msg: " + buffer[0]);
                        break;
                }
            }
        }
    };

    /**
     * Pops up an error dialog using the listen sound
     * @param message error message
     */
    public void errorDialog(String message) {
        MediaPlayer mediaPlayer = MediaPlayer.create(SeatActivity.this, R.raw.listen);

        final Dialog dialog = new Dialog(SeatActivity.this);
        dialog.setContentView(R.layout.confirm_dialog);
        dialog.setTitle(ERROR_TITLE);
        TextView textView = (TextView) dialog.findViewById(R.id.dialogText);
        textView.setText(message);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.start();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.release();
        }

    }


    /**
     * Pops up an error dialog using the listen sound, soft refreshes after tapping confirm
     * @param message error message
     */
    public void errorRefreshDialog(String message) {
        MediaPlayer mediaPlayer = MediaPlayer.create(SeatActivity.this, R.raw.listen);

        final Dialog dialog = new Dialog(SeatActivity.this);
        dialog.setContentView(R.layout.confirm_dialog);
        dialog.setTitle(ERROR_TITLE);
        TextView textView = (TextView) dialog.findViewById(R.id.dialogText);
        textView.setText(message);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seatUpdateRefresh();
                dialog.dismiss();
            }
        });

        dialog.show();
        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.start();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.release();
        }
    }


    /**
     * Pops a message that the seat has been reserved with a look sound, hard refreshes after tapping okay
     * @param r red
     * @param g green
     * @param b blue
     */
    public void reservedDialog(int r, int g, int b) {
        MediaPlayer mediaPlayer = MediaPlayer.create(SeatActivity.this, R.raw.look);

        final Dialog dialog = new Dialog(SeatActivity.this);
        dialog.setContentView(R.layout.reserved_dialog);
        dialog.setTitle("Reservation Successful");
        ImageView colorImage = (ImageView) dialog.findViewById(R.id.image);
        colorImage.setBackgroundColor(Color.rgb(r, g, b));
        TextView textView = (TextView) dialog.findViewById(R.id.reservedText);
        textView.setText("Your seat has been reserved.\nPlease look for the light with the color displayed.");

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hardRefresh();
                dialog.dismiss();
            }
        });

        dialog.show();
        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.start();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.release();
        }
    }

    /**
     * Adds one row to layoutName (Adds a horizontal LinearLayout).
     * @param layoutName linear layout to add row to
     * @param rownumber the next row number of the linear layout
     * @return LinearLayout
     */
    public LinearLayout addLayoutRow(LinearLayout layoutName, int rownumber) {
        LinearLayout layoutRow = new LinearLayout(this);
        layoutRow.setOrientation(LinearLayout.HORIZONTAL);
        layoutRow.setGravity(Gravity.CENTER_HORIZONTAL);
        switch (rownumber) {
            case 1:
                layoutRow.setId(R.id.layoutRow1);
                break;
            case 2:
                layoutRow.setId(R.id.layoutRow2);
                break;
            case 3:
                layoutRow.setId(R.id.layoutRow3);
                break;
            case 4:
                layoutRow.setId(R.id.layoutRow4);
                break;
            case 5:
                layoutRow.setId(R.id.layoutRow5);
        }
        layoutName.addView(layoutRow);
        return layoutRow;
    }

    /**
     * Adds seat image to layoutName. Meant to take the return of generateSeatImage methods and add it into
     * the layout generated by addLayoutRow
     * @param imageSeat an ImageView of the seat
     * @param layoutName a linear layout
     */
    public void addSeatImage(ImageView imageSeat, LinearLayout layoutName) {
        layoutName.addView(imageSeat);
    }

    /**
     * If seat is available, allow selections on screen, deselect when it is already selected
     * If seat is not available, allow deselect only
     * @param imageSeat SeatImageView
     */
    public void imageSelection(SeatImageView imageSeat){
        Drawable d = getResources().getDrawable(R.drawable.unavailable_seat);
        ReserveSeatsController rsv = ReserveSeatsController.getInstance(SeatActivity.this);
        d.setColorFilter(rsv.getCurrentPossibleColors2(), PorterDuff.Mode.MULTIPLY);
        NumberPicker pickerGroupSize = (NumberPicker) findViewById(R.id.pickerGroupSize);
        int groupSize = pickerGroupSize.getValue();
        Button reserveButton = (Button) findViewById(R.id.buttonReserve);

        if(imageSeat.isAvailable() && !imageSeat.isReserved()) {
            if(touchSelection == null){
                if(seatFormation == null) {
                    touchSelection = new ArrayList<Seat>();
                } else{
                    touchSelection = new ArrayList<Seat>(Arrays.asList(stripUnavailableFromFormation(seatFormation[selectedFormationIndex])));
                }
            }
            if (touchSelection.size() < groupSize) {
                if (touchSelection.contains(imageSeat.getSeat())) {
                    touchSelection.remove(imageSeat.getSeat());
                    imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.available_seat));

                } else {
                    touchSelection.add(imageSeat.getSeat());
                    imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.select_seat));
                    imageSeat.setImageDrawable(d);
                }
            } else if(touchSelection.size() == 1){
                if (touchSelection.contains(imageSeat.getSeat())) {
                    touchSelection.remove(imageSeat.getSeat());
                    imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.available_seat));

                } else {
                    touchSelection.clear();
                    touchSelection.add(imageSeat.getSeat());
                    imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.select_seat));
                    imageSeat.setImageDrawable(d);
                    seatUpdateRefresh();
//                    reserveButton.setEnabled(true);
                }

            } else {
                if (touchSelection.contains(imageSeat.getSeat())) {
                    touchSelection.remove(imageSeat.getSeat());
                    imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.available_seat));
//                    if(touchSelection.size()<1) {
//                        reserveButton.setEnabled(false);
//                    }

                }
            }
            if(touchSelection.size() != groupSize){
                reserveButton.setEnabled(false);
            } else{
                reserveButton.setEnabled(true);
            }
        }
    }

    /**
     * Generates a seat image based off the information of a single seat. Takes the information of
     * whether it is available or not and makes a distinguishable difference between available and unavailable
     *
     * @param seat a seat
     * @return ImageView
     */
    public SeatImageView generateSeatStatusImage(Seat seat) {
        final SeatImageView imageSeat = new SeatImageView(this, seat);


        imageSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable d = getResources().getDrawable(R.drawable.unavailable_seat);
                ReserveSeatsController rsv = ReserveSeatsController.getInstance(SeatActivity.this);
                d.setColorFilter(rsv.getCurrentPossibleColors2(), PorterDuff.Mode.MULTIPLY);
                if(seatFormation == null){
                    imageSelection(imageSeat);
                } else{ //if a formation is already selected
                    if(touchSelection == null) {
//                        touchSelection = new ArrayList<Seat>(Arrays.asList(seatFormation[selectedFormationIndex]));
//                        touchSelection = new ArrayList<Seat>(Arrays.asList(stripUnavailableFromFormation(seatFormation[selectedFormationIndex])));
                    }
                    imageSelection(imageSeat);
                }
            }
        });


        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(width, height);
        layoutParams1.setMargins(seatMargin, seatMargin, seatMargin, seatMargin);
        imageSeat.setLayoutParams(layoutParams1);
        if (seat.isAvailable() && !seat.getReserved()) {
            imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.available_seat));
        } else {
            imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.unavailable_seat));
        }
        return imageSeat;
    }

    /**
     * Generates a seat image based off the information of a single seat. Takes the information of
     * whether the seat is the one selected in the formation
     * @param seat a seat
     * @return ImageView
     */
    public SeatImageView generateSeatSelectedImage(Seat seat) {
        final SeatImageView imageSeat = new SeatImageView(this, seat);
        final Drawable d = getResources().getDrawable(R.drawable.unavailable_seat);
        ReserveSeatsController rsv = ReserveSeatsController.getInstance(SeatActivity.this);
        d.setColorFilter(rsv.getCurrentPossibleColors2(), PorterDuff.Mode.MULTIPLY);

        imageSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touchSelection == null) {
                    touchSelection = new ArrayList<Seat>(Arrays.asList(seatFormation[selectedFormationIndex]));
                }
                imageSelection(imageSeat);
            }
        });

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(width, height);
        layoutParams1.setMargins(seatMargin, seatMargin, seatMargin, seatMargin);
        imageSeat.setLayoutParams(layoutParams1);
        if (seat.isAvailable() && !seat.getReserved()) {
            imageSeat.setImageDrawable(d);
        } else {
            imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.unavailable_seat));
        }

        return imageSeat;
    }


    /**
     *  Displays current seats based off the multidimensional array of seats information.
     * @param seats available seats
     * @return linear layout of seats
     */
    public LinearLayout[] displaySeats(int[][] seats) {
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        ReserveSeatsController rsv = ReserveSeatsController.getInstance(this);

        Seat available;
        Seat unavailable;

        for (int row = 0; row < MAX_ROW; row++) {
            tempLayout[row] = addLayoutRow(layoutRows, row);
            for (int column = 0; column < MAX_COLUMN; column++) {
                if (seats[row][column] == 1) {
                    available = new Seat(true , rsv.getCurrentPossibleColors(), row, column);
                    addSeatImage(generateSeatStatusImage(available), tempLayout[row]);
                } else {
                    unavailable = new Seat(false , rsv.getCurrentPossibleColors(), row, column);
                    addSeatImage(generateSeatStatusImage(unavailable), tempLayout[row]);
                }

            }
        }
        return tempLayout;
    }


    /**
     * Displays one formation from a given index. Generates each seat and checks each seat in the formation
     * to see if it matches the row and column of the current seat
     * @param formation The seat formations
     * @param formIndex The formation number
     * @return a linear layout of the formation
     */
    public LinearLayout[] displayFormation(Seat[][] formation, int formIndex){
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        int r;
        int c;
        Seat unavailable;
        Seat available;
        boolean seatMade;
        ReserveSeatsController rsv = ReserveSeatsController.getInstance(this);

        for (int row = 0; row < MAX_ROW; row++) { //create a row
            tempLayout[row] = addLayoutRow(layoutRows, row);
            for (int column = 0; column < MAX_COLUMN; column++) { //create a new column
                seatMade = false;
                for(int seatInFormation = 0; seatInFormation < formation[formIndex].length; seatInFormation++){
                //go through one formation based off the formIndex to find a seat matching the current iteration of row and column
                    if(formation[formIndex][seatInFormation] != null) { // If there's an actual seat, temporarily save the row and column
                        r = formation[formIndex][seatInFormation].getRow();
                        c = formation[formIndex][seatInFormation].getCol();
                        if ((r == row) && (c == column)) { // If the row and column is matches this iteration of row and columns
                            //If this seat is in the seat formation, generate a view that indicates it's part of the formation
                            if (formation[formIndex][seatInFormation].isAvailable() && !formation[formIndex][seatInFormation].getReserved()) {
                                addSeatImage(generateSeatSelectedImage(formation[formIndex][seatInFormation]), tempLayout[row]);
                                seatMade = true;
                            }

                        }
                    }
                }
                // If this seat is not in the seat formation, check if it is one of the available seats
                // and indicate whether it's an available seat or not
                if(seatMade == false) {
                    if (availableSeats[row][column] == 1) {
                        available = new Seat(true , rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(available), tempLayout[row]);
                    } else {
                        unavailable = new Seat(false , rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(unavailable), tempLayout[row]);
                    }
                }
            }
        }
        return tempLayout;
    }


    /**
     * Displays one formation from a given index. Generates each seat and checks each seat in the formation
     * to see if it matches the row and column of the current seat
     * @param formation The seat formations
     * @return a linear layout of the formation
     */
    public LinearLayout[] displaySelection(ArrayList<Seat> formation){
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        int r;
        int c;
        Seat unavailable;
        Seat available;
        boolean seatMade;
        ReserveSeatsController rsv = ReserveSeatsController.getInstance(this);

        for (int row = 0; row < MAX_ROW; row++) { //create a row
            tempLayout[row] = addLayoutRow(layoutRows, row);
            for (int column = 0; column < MAX_COLUMN; column++) { //create a new column
                seatMade = false;
                for(int seatInFormation = 0; seatInFormation < formation.size(); seatInFormation++){
                    //go through one formation based off the formIndex to find a seat matching the current iteration of row and column
                    if(formation.get(seatInFormation) != null) { // If there's an actual seat, temporarily save the row and column
                        r = formation.get(seatInFormation).getRow();
                        c = formation.get(seatInFormation).getCol();
                        if ((r == row) && (c == column)) { // If the row and column is matches this iteration of row and columns
                            //If this seat is in the seat formation, generate a view that indicates it's part of the formation
                            if (formation.get(seatInFormation).isAvailable() && !formation.get(seatInFormation).getReserved()) {
                                addSeatImage(generateSeatSelectedImage(formation.get(seatInFormation)), tempLayout[row]);
                                seatMade = true;
                            }

                        }
                    }
                }
                // If this seat is not in the seat formation, check if it is one of the available seats
                // and indicate whether it's an available seat or not
                if(seatMade == false) {
                    if (availableSeats[row][column] == 1) {
                        available = new Seat(true , rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(available), tempLayout[row]);
                    } else {
                        unavailable = new Seat(false , rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(unavailable), tempLayout[row]);
                    }
                }
            }
        }
        return tempLayout;
    }


    public LinearLayout[] displayFormation(ArrayList<ArrayList<Seat>> formation, int formIndex){
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        int r;
        int c;
        Seat unavailable;
        Seat available;
        boolean seatMade;
        ReserveSeatsController rsv = ReserveSeatsController.getInstance(this);

        for (int row = 0; row < MAX_ROW; row++) { //create a row
            tempLayout[row] = addLayoutRow(layoutRows, row);
            for (int column = 0; column < MAX_COLUMN; column++) { //create a new column
                seatMade = false;

                for(int seatInFormation = 0; seatInFormation <formation.get(formIndex).size(); seatInFormation++){
                    //go through one formation based off the formIndex to find a seat matching the current iteration of row and column
                    if(formation.get(formIndex).get(seatInFormation) != null) { // If there's an actual seat, temporarily save the row and column
                        r = formation.get(formIndex).get(seatInFormation).getRow();
                        c = formation.get(formIndex).get(seatInFormation).getCol();
                        if ((r == row) && (c == column)) { // If the row and column is matches this iteration of row and columns
                            //If this seat is in the seat formation, generate a view that indicates it's part of the formation
                            if (formation.get(formIndex).get(seatInFormation).isAvailable() && !formation.get(formIndex).get(seatInFormation).getReserved()) {
                                addSeatImage(generateSeatSelectedImage(formation.get(formIndex).get(seatInFormation)), tempLayout[row]);
                                seatMade = true;
                            }

                        }
                    }
                }
                // If this seat is not in the seat formation, check if it is one of the available seats
                // and indicate whether it's an available seat or not
                if(seatMade == false) {
                    if (availableSeats[row][column] == 1) {
                        available = new Seat(true , rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(available), tempLayout[row]);
                    } else {
                        unavailable = new Seat(false , rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(unavailable), tempLayout[row]);
                    }
                }
            }
        }
        return tempLayout;
    }

    /**
     * Refreshes the current available seats layout and removes the current formations
     */
    public void softRefresh() {
        availableSeats = DBController.getController().getAvailableSeatsInt();
        tempLinLayout = displaySeats(availableSeats);

        seatFormation = null;
        touchSelection = null;

        Button rightButton = (Button) findViewById(R.id.buttonRight);
        Button leftButton = (Button) findViewById(R.id.buttonLeft);
        Button reserveButton = (Button) findViewById(R.id.buttonReserve);
        rightButton.setEnabled(false);
        leftButton.setEnabled(false);
        reserveButton.setEnabled(false);

        selectedFormationIndex = 0;
    }

    /**
     * Refreshes to update freed up available seats without losing hand selected formations
     */
    public void seatUpdateRefresh() {
        availableSeats = DBController.getController().getAvailableSeatsInt();
        if((seatFormation == null) && (touchSelection == null)){
            tempLinLayout = displaySeats(availableSeats);
        } else {
            if(touchSelection != null){
                tempLinLayout = displaySelection(touchSelection);
            }
            else if(seatFormation != null) {
                tempLinLayout = displayFormation(seatFormation, selectedFormationIndex);
            }
        }
    }

    /**
     * Refreshes the current available seats layout and user preferences
     */
    public void hardRefresh() {
        availableSeats = DBController.getController().getAvailableSeatsInt();
        tempLinLayout = displaySeats(availableSeats);
        seatFormation = null;
        touchSelection = null;

        Button rightButton = (Button) findViewById(R.id.buttonRight);
        Button leftButton = (Button) findViewById(R.id.buttonLeft);
        Button reserveButton = (Button) findViewById(R.id.buttonReserve);
        rightButton.setEnabled(false);
        leftButton.setEnabled(false);
        reserveButton.setEnabled(false);

        selectedFormationIndex = 0;

        final RadioGroup seatPreferences = (RadioGroup) findViewById(R.id.seatPreferences);
        seatPreferences.check(R.id.prefNone);

        final NumberPicker pickerGroupSize = (NumberPicker) findViewById(R.id.pickerGroupSize);
        pickerGroupSize.setMaxValue(MAX_GROUP_SIZE);
        pickerGroupSize.setMinValue(1);
        pickerGroupSize.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerGroupSize.setValue(1);

    }


    /**
     * For testing purposes only, generates formations based off group size and seats available
     * @param groupSize the group size
     * @return Seat formations
     */
    public Seat[][] randomSeatFormation(int groupSize) {
        Random random = new Random();
        int numberOfFormations = 1;
        int seatIndex;
        int size;
        int remaining;
        Seat unavailable;
        Seat available;
        Seat[][] seats = new Seat[numberOfFormations][(MAX_ROW*MAX_COLUMN)];

        ReserveSeatsController rsv = ReserveSeatsController.getInstance(this);

        for (int formIndex = 0; formIndex < numberOfFormations; formIndex++) {
            seatIndex = 0;
            remaining = calculateNumberOfAvailableSeats();
            size = 0;
            for (int r = 0; r < MAX_ROW; r++) {
                for (int c = 0; c < MAX_COLUMN; c++) {
                    //if there's not enough selected
                    if((size < groupSize) && (availableSeats[r][c] == 1) ) {
                        if(remaining < (groupSize-size)){
                            available = new Seat(true , rsv.getCurrentPossibleColors(), r, c);
                            available.setCol(c);
                            available.setRow(r);
                            seats[formIndex][seatIndex] = available;
                            size++;
                            seatIndex++;
                        }
                        else {
                            if(random.nextBoolean()){
                                available = new Seat(true , rsv.getCurrentPossibleColors(), r, c);
                                available.setCol(c);
                                available.setRow(r);
                                seats[formIndex][seatIndex] = available;
                                size++;
                                seatIndex++;
                            } else{
//                                unavailable = new Seat(false , rsv.getCurrentPossibleColors(), r, c);
//                                unavailable.setCol(c);
//                                unavailable.setRow(r);
//                                seats[formIndex][seatIndex] = unavailable;
                            }
                        }
                        remaining--;
                    }
                    else{
//                        unavailable = new Seat(false , rsv.getCurrentPossibleColors(), r, c);
//                        unavailable.setCol(c);
//                        unavailable.setRow(r);
//                        seats[formIndex][seatIndex] = unavailable;
                    }

//                    seatIndex++;
                }
            }
        }
        System.out.print("indexlength:" + seats.length);
        return seats;
    }

    /**
     * Removes null or unavailable seats from current formation
     * @param formation seat formation
     * @return
     */
    public Seat[] stripUnavailableFromFormation(Seat[] formation){
        ArrayList<Seat> availables = new ArrayList<Seat>();
        for(Seat seat : formation){
            if(seat == null){}
            else if(seat.isAvailable() && !seat.getReserved()){
                availables.add(seat);
            }
        }
        return availables.toArray(new Seat[availables.size()]);
    }

    /**
     * Removes null or unavailable seats from current formation
     * @param formation seat formation
     * @return formation with no nulls
     */
    public ArrayList<ArrayList<Seat>> stripNullFormation(Seat[][] formation){
        ArrayList<ArrayList<Seat>> availables = new ArrayList<ArrayList<Seat>>();
        for(int i = 0; i < formation.length; i++){
            if(formation[i] == null){}
            else{
                ArrayList<Seat> a = new ArrayList<Seat>();
                for(int j = 0; j < formation[i].length; j++){
                    if(formation[i][j]!= null){
                        a.add(formation[i][j]);
                    }
                }
                availables.add(a);
            }
        }
        if(availables.size() == 0){
            availables = null;
        }
        return availables;
    }

    /**
     * Rotates the seat formation index
     */
    private boolean formationIndexRight(){
        boolean success = false ;
        if(selectedFormationIndex < (seatFormation.length-1)){
            selectedFormationIndex++;
            success = true;
        }
        else{
            selectedFormationIndex = 0;
            success = true;
        }
        return success;
    }


    /**
     * Rotates the seat formation index
     */
    private boolean formationIndexLeft(){
        boolean success = false;
        if(selectedFormationIndex != 0){
            selectedFormationIndex--;
            success = true;
        }
        else{
            selectedFormationIndex = seatFormation.length-1;
            success = true;
        }
        return success;
    }

    private int calculateNumberOfAvailableSeats(){
        int count = 0;
        for (int r = 0; r < MAX_ROW; r++) {
            for (int c = 0; c < MAX_COLUMN; c++) {
                if(availableSeats[r][c] == 1){
                    count++;
                }
            }
        }
        return count;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getMAX_COLUMN() {
        return MAX_COLUMN;
    }

    public int getMAX_ROW() {
        return MAX_ROW;
    }

    public int getTOTAL_SEATS() {
        return TOTAL_SEATS;
    }

    public int getMAX_GROUP_SIZE() {
        return MAX_GROUP_SIZE;
    }

    public int[] getReserveColor() {
        return reserveColor;
    }

    public int[][] getAvailableSeats() {
        return availableSeats;
    }

    public Seat[][] getSeatFormation() {
        return seatFormation;
    }

    public Seat[] getSelectedFormation() {
        return selectedFormation;
    }

    public int getSelectedFormationIndex() {
        return selectedFormationIndex;
    }

    public void setSelectedFormation(Seat[] selectedFormation) {
        this.selectedFormation = selectedFormation;
    }

    public void setSelectedFormationIndex(int selectedFormationIndex) {
        this.selectedFormationIndex = selectedFormationIndex;
    }

}