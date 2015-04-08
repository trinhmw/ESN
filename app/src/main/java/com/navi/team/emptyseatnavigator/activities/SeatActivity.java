package com.navi.team.emptyseatnavigator.activities;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;

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

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.businessobject.InputController;
import com.navi.team.emptyseatnavigator.businessobject.ReserveSeatsController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


public class SeatActivity extends ActionBarActivity {

    private final static String TAG = SeatActivity.class.getSimpleName();

    private final int MAX_COLUMN = 4;
    private final int MAX_ROW = 3;
    private final int TOTAL_SEATS = MAX_COLUMN * MAX_ROW;
    private final int MAX_GROUP_SIZE = 4;
    private int[] reserveColor;
    private Seat[][] availableSeats = new Seat[MAX_ROW][MAX_COLUMN];
    private Seat[][] seatFormation;
    private Seat[] selectedFormation;
    private int selectedFormationIndex = 0;
    private LinearLayout[] tempLinLayout;
    private final String ERROR_TITLE = "Hey Listen!";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_launcher);
        }

        // Group size picker
        final NumberPicker pickerGroupSize = (NumberPicker) findViewById(R.id.pickerGroupSize);
        pickerGroupSize.setMaxValue(MAX_GROUP_SIZE);
        pickerGroupSize.setMinValue(1);
        pickerGroupSize.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

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
                    seatFormation = null;
//                    seatFormation = InputController.validateInput(
//                            pickerGroupSize.getValue(),
//                            preference.getText().toString(),
//                            MAX_GROUP_SIZE);
                    if (seatFormation != null) {
                        displayFormation(seatFormation,0);
                    } else {
                        errorDialog("No seat formations available.");
                    }
                }
            }
        });

        final Button reserveButton = (Button) findViewById(R.id.buttonReserve);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReserveSeatsController rsc = ReserveSeatsController.getInstance(getApplicationContext());

                seatFormation = availableSeats;
                selectedFormation = seatFormation[selectedFormationIndex];
                reserveColor = rsc.reserveSeats(selectedFormation);
                if (!(reserveColor[0] == 0 && reserveColor[1] == 0 && reserveColor[2] == 0)) {
                    //Show reservation seat color with a confirmation button
                    //Clear the display to available seats again
                    reservedDialog(reserveColor[0], reserveColor[1], reserveColor[2]);
                } else {
                    //Tell the user that their seats could not be reserved and refresh display back to available seats
                    errorRefreshDialog("Your seats have been taken, please try again.");
                }
            }
        });

        randomSeatAvailability(availableSeats);
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

    // Determine when Arduino is attached and if permission is granted by user
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
                        final Seat cmdSeat = new Seat(col, row, isAvailable);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update map with cmdSeat; refresh UI
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

    public void errorDialog(String message) {
        MediaPlayer mediaPlayer = MediaPlayer.create(SeatActivity.this, R.raw.listen);

        Dialog dialog = new Dialog(SeatActivity.this);
        dialog.setContentView(R.layout.error_dialog);
        dialog.setTitle(ERROR_TITLE);
        TextView textView = (TextView) dialog.findViewById(R.id.text);
        textView.setText(message);
        dialog.show();
        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.start();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.release();
        }

    }


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
                refresh();
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



    public void reservedDialog(int r, int g, int b) {
        MediaPlayer mediaPlayer = MediaPlayer.create(SeatActivity.this, R.raw.look);

        final Dialog dialog = new Dialog(SeatActivity.this);
        dialog.setContentView(R.layout.reserved_dialog);
        dialog.setTitle("Reservation Successful");
        ImageView colorImage = (ImageView) dialog.findViewById(R.id.image);
        colorImage.setBackgroundColor(Color.rgb(r, g, b));
        TextView textView = (TextView) dialog.findViewById(R.id.reservedText);
        textView.setText("Your seat has been reserved.\nPlease look for the light with the color displayed");

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
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
     * addLayoutRow
     * Adds one row to layoutName (Adds a horizontal LinearLayout).
     *
     * @param layoutName
     * @param rownumber
     * @return LinearLayout
     */
    public LinearLayout addLayoutRow(LinearLayout layoutName, int rownumber) {
        LinearLayout layoutRow = new LinearLayout(this);
        layoutRow.setOrientation(LinearLayout.HORIZONTAL);
        layoutRow.setGravity(Gravity.CENTER_HORIZONTAL);
//        LinearLayout.LayoutParams param = new ActionMenuView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
     * addSeatButton
     * Adds button to layoutName. Meant to take the return of generateSeatButton method and add it into
     * the layout generated by addLayoutRow
     *
     * @param button
     * @param layoutName
     */
    public void addSeatButton(Button button, LinearLayout layoutName) {
        layoutName.addView(button);
    }

    /**
     * generateSeatStatusButton
     * Generates a seat button based off the information of a single seat. Takes the information of
     * whether it is available or not and makes a distinguishable difference between available and unavailable
     *
     * @param seat
     * @return Button
     */
    public Button generateSeatStatusButton(Seat seat) {
        Resources res = getResources();
        Button buttonSeat = new Button(this);
        int height = 50;
        int width = 50;
        buttonSeat.setId(R.id.tempbutton);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(width, height);
        layoutParams1.setMargins(3, 3, 3, 3);
        buttonSeat.setLayoutParams(layoutParams1);
        if (seat.isAvailable()) {
            buttonSeat.getBackground().setColorFilter(res.getColor(R.color.empty), PorterDuff.Mode.MULTIPLY);
        } else {
            buttonSeat.setEnabled(false);
        }
        return buttonSeat;
    }

    /**
     * generateSeatSelectedButton
     * Generates a seat button based off the information of a single seat. Takes the information of
     * whether the seat is the one selected in the formation
     *
     * @param seat
     * @return Button
     */
    public Button generateSeatSelectedButton(Seat seat) {
        Resources res = getResources();
        Button buttonSeat = new Button(this);
        int height = 50;
        int width = 50;
        buttonSeat.setId(R.id.tempbutton);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(width, height);
        layoutParams1.setMargins(3, 3, 3, 3);
        buttonSeat.setLayoutParams(layoutParams1);
        if (seat.isAvailable()) {
            buttonSeat.getBackground().setColorFilter(res.getColor(R.color.not_empty), PorterDuff.Mode.MULTIPLY);
        } else {
            buttonSeat.setEnabled(false);
        }

        return buttonSeat;
    }

    /**
     * displaySeats
     * Displays current seats based off the multidimensional array of seats information.
     *
     * @param seats
     * @param
     * @return
     */
    public LinearLayout[] displaySeats(Seat[][] seats) {
        LinearLayout layoutSeat = (LinearLayout) findViewById(R.id.layoutSeat);
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();

        for (int i = 0; i < MAX_ROW; i++) {
            tempLayout[i] = addLayoutRow(layoutRows, i);
            for (int j = 0; j < MAX_COLUMN; j++) {

                    addSeatButton(generateSeatStatusButton(seats[i][j]), tempLayout[i]);
            }
        }
        return tempLayout;
    }


    public LinearLayout[] displaySeats(int[][] seats) {
        LinearLayout layoutSeat = (LinearLayout) findViewById(R.id.layoutSeat);
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();

        Seat available = new Seat(true);
        Seat unavailable = new Seat(false);

        for (int row = 0; row < MAX_ROW; row++) {
            tempLayout[row] = addLayoutRow(layoutRows, row);
            for (int column = 0; column < MAX_COLUMN; column++) {
                if (seats[row][column] == 1) {
                    addSeatButton(generateSeatSelectedButton(available), tempLayout[row]);
                } else {
                    addSeatButton(generateSeatSelectedButton(unavailable), tempLayout[row]);
                }

            }
        }
        return tempLayout;
    }


    /**
     * displayFormation
     * Displays one formation from a given index. Generates each seat and checks each seat in the formation
     * to see if it matches the row and column of the current seat
     * @param formation - The seat formations
     * @param formIndex - The formation number
     * @return
     */
    public LinearLayout[] displayFormation(Seat[][] formation, int formIndex){
        LinearLayout layoutSeat = (LinearLayout) findViewById(R.id.layoutSeat);
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        int r;
        int c;
        Seat unavailable = new Seat(false);

        for (int row = 0; row < MAX_ROW; row++) {
            tempLayout[row] = addLayoutRow(layoutRows, row);
            for (int column = 0; column < MAX_COLUMN; column++) {
                for(int seatInFormation = 0; seatInFormation < formation[formIndex].length; seatInFormation++){
                    r = formation[formIndex][seatInFormation].getRow();
                    c = formation[formIndex][seatInFormation].getCol();
                    if((r == row) && (c == column)){
                        addSeatButton(generateSeatSelectedButton(formation[formIndex][seatInFormation]), tempLayout[row]);
                    }
                    else{
                        addSeatButton(generateSeatSelectedButton(unavailable), tempLayout[row]);
                    }
                }
            }
        }
        return tempLayout;
    }

    public void setAvailableSeats(Seat[][] availableSeats) {
        this.availableSeats = availableSeats;
    }

    public void setSeatFormation(Seat[][] seatFormation) {
        this.seatFormation = seatFormation;
    }

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * randomSeatAvailability
     * For testing purposes only
     */
    public void randomSeatAvailability(Seat seats[][]) {
        Random random = new Random();
        for (int i = 0; i < MAX_ROW; i++) {
            for (int j = 0; j < MAX_COLUMN; j++) {
                seats[i][j] = new Seat(random.nextBoolean());
            }
        }
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
}