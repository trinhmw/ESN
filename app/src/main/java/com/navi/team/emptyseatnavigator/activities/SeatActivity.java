package com.navi.team.emptyseatnavigator.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.support.v4.app.FragmentActivity;
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


public class SeatActivity extends FragmentActivity implements Constants, SeatDisplayFragment.OnFragmentInteractionListener, PreferenceFragment.OnFragmentInteractionListener{

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
    private int groupSize;

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
    private FragmentManager fm = null;
    private FragmentTransaction ft = null;
    private PreferenceFragment preferenceFragment;
    private SeatDisplayFragment seatDisplayFragment;
    private int container;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        seatActivity = this;
        availableSeats = DBController.getController().getAvailableSeatsInt();
        container = R.id.fragment_container;

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            PreferenceFragment preferenceFragment = new PreferenceFragment();
            Bundle prefBundle = new Bundle();
            prefBundle.putSerializable("availableSeats", availableSeats);
            preferenceFragment.setArguments(prefBundle);
            ft.add(R.id.fragment_container, preferenceFragment).commit();
        }



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
                                availableSeats = DBController.getController().getAvailableSeatsInt();
//                                seatUpdateRefresh();
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
     * Refreshes the current available seats layout and user preferences
     */
    public void hardRefresh() {
        availableSeats = DBController.getController().getAvailableSeatsInt();
//        tempLinLayout = displaySeats(availableSeats);
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

    public void swapToSeatDisplayFragment(){
        availableSeats = DBController.getController().getAvailableSeatsInt();
        fm = getFragmentManager();
        ft = fm.beginTransaction();
        seatDisplayFragment = new SeatDisplayFragment();
        Bundle seatDisplayBundle = new Bundle();
        seatDisplayBundle.putSerializable("availableSeats",availableSeats);
        seatDisplayBundle.putInt("selectedFormationIndex",selectedFormationIndex);
        seatDisplayBundle.putInt("groupSize", groupSize);
        seatDisplayBundle.putSerializable("seatFormation", seatFormation);
        seatDisplayFragment.setArguments(seatDisplayBundle);
        ft.replace(R.id.fragment_container, seatDisplayFragment);
        ft.addToBackStack("Submitted seat preferences.");
        ft.commit();

    }

    public void swapToPreferenceFragment(){
        availableSeats = DBController.getController().getAvailableSeatsInt();
        fm = getFragmentManager();
        ft = fm.beginTransaction();
        PreferenceFragment preferenceFragment = new PreferenceFragment();
        Bundle prefBundle = new Bundle();
        prefBundle.putSerializable("availableSeats", availableSeats);
        preferenceFragment.setArguments(prefBundle);
        ft.replace(R.id.fragment_container, preferenceFragment);
        ft.addToBackStack("Completed Reservation.");
        ft.commit();

    }

    @Override
    public void onSubmit(Seat[][] seatFormation, int groupSize) {
        this.seatFormation = seatFormation;
        this.selectedFormationIndex = 0;
        this.groupSize = groupSize;

        if(seatFormation != null){
            swapToSeatDisplayFragment();
        }
    }

    @Override
    public void onMakeReservation() {
        swapToPreferenceFragment();
    }
}