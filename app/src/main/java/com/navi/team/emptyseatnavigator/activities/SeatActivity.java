package com.navi.team.emptyseatnavigator.activities;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.businessobject.ReserveSeatsController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;

import java.util.ArrayList;
import java.util.List;


public class SeatActivity extends ActionBarActivity {
    private final int MAX_COLUMN = 4;
    private final int MAX_ROW = 2;
    private final int TOTAL_SEATS = 8;
    private final int MAX_GROUP_SIZE = 4;
    private Seat[][] availableSeats;
    private Seat[][] seatFormation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        Spinner spinnerSeatFormation = (Spinner) findViewById(R.id.spinnerSeatFormation);
        List<String> list = new ArrayList<>();
        list.add("Formation 1");
        list.add("Formation 2");
        list.add("Formation 3");
        ArrayAdapter<String> adapterSeatFormation = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapterSeatFormation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeatFormation.setAdapter(adapterSeatFormation);

        NumberPicker pickerGroupSize = (NumberPicker) findViewById(R.id.pickerGroupSize);
        pickerGroupSize.setMaxValue(MAX_GROUP_SIZE);
        pickerGroupSize.setMinValue(1);

        LinearLayout layoutSeat = (LinearLayout) findViewById(R.id.layoutSeat);
        LinearLayout layoutRows = (LinearLayout) findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        Button temp = generateSeatButton(new Seat(true));

        for(int i = 0; i<MAX_ROW; i++)
        {
            tempLayout[i] = addLayoutRow(layoutRows,i);
        }

        temp = generateSeatButton(new Seat(true));
        addSeatButton(temp, tempLayout[0]);

        temp = generateSeatButton(new Seat(true));
        addSeatButton(temp, tempLayout[0]);

        temp = generateSeatButton(new Seat(false));
        addSeatButton(temp, tempLayout[0]);

        temp = generateSeatButton(new Seat(true));
        addSeatButton(temp, tempLayout[0]);

        temp = generateSeatButton(new Seat(true));
        addSeatButton(temp, tempLayout[1]);

        temp = generateSeatButton(new Seat(false));
        addSeatButton(temp, tempLayout[1]);

        temp = generateSeatButton(new Seat(true));
        addSeatButton(temp, tempLayout[1]);

        temp = generateSeatButton(new Seat(false));
        addSeatButton(temp, tempLayout[1]);

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
     * generateSeatButton
     * Generates a seat button based off the information of a single seat. Takes the information of
     * whether it is available or not and makes a distinguishable difference between available and unavailable
     * @param seat
     * @return Button
     */
    public Button generateSeatButton(Seat seat){
        Resources res = getResources();
        Button buttonSeat = new Button(this);
        buttonSeat.setId(R.id.tempbutton);
        if(seat.isAvailable()){
//            buttonSeat.setEnabled(false);
            buttonSeat.getBackground().setColorFilter(res.getColor(R.color.empty), PorterDuff.Mode.MULTIPLY);
        }
        else{
            buttonSeat.setEnabled(false);
            buttonSeat.getBackground().setColorFilter(res.getColor(R.color.not_empty), PorterDuff.Mode.MULTIPLY);
        }
        buttonSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                Resources res = getResources();
                ReserveSeatsController rsc = new ReserveSeatsController(null);
                int[] colors = rsc.hexToRGB(res.getColor(R.color.not_empty));

                Toast.makeText(getApplicationContext(), "R:" + colors[0] + ", G:" + colors[1] + ",B:" + colors[2], Toast.LENGTH_LONG).show();

            }
        });
        return buttonSeat;
    }
}
