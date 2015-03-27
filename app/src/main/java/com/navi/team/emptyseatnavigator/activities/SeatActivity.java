package com.navi.team.emptyseatnavigator.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;

import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.businessobject.InputController;
import com.navi.team.emptyseatnavigator.businessobject.ReserveSeatsController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SeatActivity extends ActionBarActivity {
    private final int MAX_COLUMN = 4;
    private final int MAX_ROW = 3;
    private final int TOTAL_SEATS = MAX_COLUMN * MAX_ROW;
    private final int MAX_GROUP_SIZE = 4;
    private int[] reserveColor;
    private Seat[][] availableSeats = new Seat[MAX_ROW][MAX_COLUMN];
    private Seat[][] seatFormation = new Seat[MAX_ROW][MAX_COLUMN];
    private Seat[][][] seatFormationSet;
    private LinearLayout[] tempLinLayout;
    private final String ERROR_TITLE = "Hey Listen!";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_launcher);
        }


//        Seat Formation Spinner
//        Spinner spinnerSeatFormation = (Spinner) findViewById(R.id.spinnerSeatFormation);
//        List<String> list = new ArrayList<>();
//        list.add("Formation 1");
//        list.add("Formation 2");
//        list.add("Formation 3");
//        ArrayAdapter<String> adapterSeatFormation = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, list);
//        adapterSeatFormation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerSeatFormation.setAdapter(adapterSeatFormation);


//        Number Picker
        final NumberPicker pickerGroupSize = (NumberPicker) findViewById(R.id.pickerGroupSize);
        pickerGroupSize.setMaxValue(MAX_GROUP_SIZE);
        pickerGroupSize.setMinValue(1);
        pickerGroupSize.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);




        Button submitButton = (Button) findViewById(R.id.buttonSubmit);
        View.OnClickListener submitOnClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//               Radio Group
                RadioGroup seatPreferencesRadioGroup = (RadioGroup) findViewById(R.id.radioGroup0);
                int checkedPreference = seatPreferencesRadioGroup.getCheckedRadioButtonId();
                boolean hasFormation;
                if(checkedPreference != -1) {
                    RadioButton radioButton = (RadioButton) findViewById(checkedPreference);
                    InputController ic = new InputController();
                    if (radioButton.getText() != null) {
                        hasFormation = ic.validateInput(pickerGroupSize.getValue(), radioButton.getText().toString(), MAX_GROUP_SIZE);
                        if(hasFormation == false){
                            errorDialog("No possible formations available");
                        }
                        //dialog here on whether seat formations are generated
                    }
                }
                else{
                    errorDialog("Please select your seat preference.");
                }
            }
        };
        submitButton.setOnClickListener(submitOnClickListener);


        final Button reserveButton = (Button) findViewById(R.id.buttonReserve);
        View.OnClickListener reserveOnClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ReserveSeatsController rsc = ReserveSeatsController.getInstance(getApplicationContext());
                seatFormation = availableSeats;
                reserveColor = rsc.reserveSeats(seatFormation);
                if(!(reserveColor[0] == 0  &&  reserveColor[1] == 0 && reserveColor[2] == 0))
                {
                    //Show reservation seat color with a confirmation button
                    //Clear the display to available seats again
                    reservedDialog(reserveColor[0],reserveColor[1],reserveColor[2]);
                }
                else{
                    //Tell the user that their seats could not be reserved and refresh display back to available seats
                    errorDialog("Your seats have been taken, please try again.");
                    refresh();
                }
            }
        };
        reserveButton.setOnClickListener(reserveOnClickListener);

        randomSeatAvailability(availableSeats);

        tempLinLayout = displaySeats(availableSeats,0);
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

    public void errorDialog(String message){
        MediaPlayer mediaPlayer = MediaPlayer.create(SeatActivity.this, R.raw.listen);

        Dialog dialog = new Dialog(SeatActivity.this);
        dialog.setContentView(R.layout.error_dialog);
        dialog.setTitle(ERROR_TITLE);
        TextView textView = (TextView) dialog.findViewById(R.id.text);
        textView.setText(message);
        dialog.show();
        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(1,1);
        mediaPlayer.start();
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.release();
        }

    }

    public void reservedDialog(int r,int g,int b){
        final Dialog dialog = new Dialog(SeatActivity.this);
        dialog.setContentView(R.layout.reserved_dialog);
        dialog.setTitle("Reservation Successful");
        ImageView colorImage = (ImageView) dialog.findViewById(R.id.image);
        colorImage.setBackgroundColor(Color.rgb(r,g,b));
        TextView textView = (TextView) dialog.findViewById(R.id.reservedText);
        textView.setText("Your seat has been reserved.\nPlease look for the light with the color displayed");

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
//                displaySeats(availableSeats,0);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * addLayoutRow
     * Adds one row to layoutName (Adds a horizontal LinearLayout).
     * @param layoutName
     * @param rownumber
     * @return LinearLayout
     */
    public LinearLayout addLayoutRow(LinearLayout layoutName, int rownumber){
        LinearLayout layoutRow = new LinearLayout(this);
        layoutRow.setOrientation(LinearLayout.HORIZONTAL);
        layoutRow.setGravity(Gravity.CENTER_HORIZONTAL);
//        LinearLayout.LayoutParams param = new ActionMenuView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        switch(rownumber){
            case 1: layoutRow.setId(R.id.layoutRow1);
                break;
            case 2: layoutRow.setId(R.id.layoutRow2);
                break;
            case 3: layoutRow.setId(R.id.layoutRow3);
                break;
            case 4: layoutRow.setId(R.id.layoutRow4);
                break;
            case 5: layoutRow.setId(R.id.layoutRow5);
        }
        layoutName.addView(layoutRow);
//        Button temp = generateSeatButton(new Seat(true));
//        addSeatButton(temp, layoutRow);
//
//        temp = generateSeatButton(new Seat(true));
//        addSeatButton(temp, layoutRow);
//
//        temp = generateSeatButton(new Seat(false));
//        addSeatButton(temp, layoutRow);

        return layoutRow;
    }

    /**
     * addSeatButton
     * Adds button to layoutName. Meant to take the return of generateSeatButton method and add it into
     * the layout generated by addLayoutRow
     * @param button
     * @param layoutName
     */
    public void addSeatButton(Button button, LinearLayout layoutName){
        layoutName.addView(button);
    }

    /**
     * generateSeatStatusButton
     * Generates a seat button based off the information of a single seat. Takes the information of
     * whether it is available or not and makes a distinguishable difference between available and unavailable
     * @param seat
     * @return Button
     */
    public Button generateSeatStatusButton(Seat seat){
        Resources res = getResources();
        Button buttonSeat = new Button(this);
        int height = 50;
        int width = 50;
        buttonSeat.setId(R.id.tempbutton);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(width, height);
        layoutParams1.setMargins(3,3,3,3);
        buttonSeat.setLayoutParams(layoutParams1);
        if(seat.isAvailable()){
//            buttonSeat.setEnabled(false);
            buttonSeat.getBackground().setColorFilter(res.getColor(R.color.empty), PorterDuff.Mode.MULTIPLY);
        }
        else{
            buttonSeat.setEnabled(false);
//            buttonSeat.getBackground().setColorFilter(res.getColor(R.color.not_empty), PorterDuff.Mode.MULTIPLY);
        }
//        buttonSeat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int id = v.getId();
//                Resources res = getResources();
//                ReserveSeatsController rsc = new ReserveSeatsController(null);
//                int[] colors = rsc.hexToRGB(res.getColor(R.color.not_empty));

//                Toast.makeText(getApplicationContext(), "R:" + colors[0] + ", G:" + colors[1] + ",B:" + colors[2], Toast.LENGTH_LONG).show();
//                 tempLinLayout[0].removeAllViews();
//                LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
//                layoutRows.removeAllViews();
//            }
//        });
        return buttonSeat;
    }

    /**
     * generateSeatSelectedButton
     * Generates a seat button based off the information of a single seat. Takes the information of
     * whether the seat is the one selected in the formation
     * @param seat
     * @return Button
     */
    public Button generateSeatSelectedButton(Seat seat){
        Resources res = getResources();
        Button buttonSeat = new Button(this);
        int height = 50;
        int width = 50;
        buttonSeat.setId(R.id.tempbutton);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(width, height);
        layoutParams1.setMargins(3,3,3,3);
        buttonSeat.setLayoutParams(layoutParams1);
        if(seat.isAvailable()){
            buttonSeat.getBackground().setColorFilter(res.getColor(R.color.not_empty), PorterDuff.Mode.MULTIPLY);
        }
        else{
            buttonSeat.setEnabled(false);
        }

        return buttonSeat;
    }

    /**
     * displaySeats
     * Displays current seats based off the multidimensional array of seats informaiton. The type determines whether it is displaying
     * seat status or the selected formation
     * @param seats
     * @param type - 0 for available seats, 1 for formation seats
     * @return
     */
    public LinearLayout[] displaySeats(Seat[][] seats, int type){
        LinearLayout layoutSeat = (LinearLayout) findViewById(R.id.layoutSeat);
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        for(int i = 0; i < MAX_ROW ;i++){
            tempLayout[i] = addLayoutRow(layoutRows,i);
            for(int j = 0; j < MAX_COLUMN;j++){
                if(type == 0) {
                    addSeatButton(generateSeatStatusButton(seats[i][j]), tempLayout[i]);
                }
                if(type == 1){
                    addSeatButton(generateSeatSelectedButton(seats[i][j]), tempLayout[i]);
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

    public void refresh(){
        Intent intent = getIntent();
        overridePendingTransition(0,0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0,0);

        startActivity(intent);
        overridePendingTransition(0,0);
    }

    /**
     * randomSeatAvailability
     * For testing purposes only
     */
    public void randomSeatAvailability(Seat seats[][]){
        Random random = new Random();
        for(int i = 0; i <MAX_ROW; i++){
            for(int j = 0; j<MAX_COLUMN; j++){
                seats[i][j] = new Seat(random.nextBoolean());
            }
        }
    }
}
