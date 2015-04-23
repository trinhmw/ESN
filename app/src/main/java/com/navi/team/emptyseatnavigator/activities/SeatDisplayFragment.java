package com.navi.team.emptyseatnavigator.activities;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.businessobject.Constants;
import com.navi.team.emptyseatnavigator.businessobject.DBController;
import com.navi.team.emptyseatnavigator.businessobject.ReserveSeatsController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;
import com.navi.team.emptyseatnavigator.businessobject.SeatImageView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SeatDisplayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SeatDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SeatDisplayFragment extends Fragment implements Constants{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int[] reserveColor;
    private int[][] availableSeats = new int[MAX_ROW][MAX_COLUMN];
    private Seat[][] seatFormation;
    private Seat[] selectedFormation;
    private ArrayList<Seat> touchSelection = null;
    private int selectedFormationIndex = 0;
    private LinearLayout[] tempLinLayout;
    private final String ERROR_TITLE = "Hey Listen!";
    private SeatActivity seatActivity;
    private int groupSize = 0;

    private final int height = 70;
    private final int width = 70;
    private final int seatMargin = 5;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SeatDisplayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SeatDisplayFragment newInstance(String param1, String param2) {
        SeatDisplayFragment fragment = new SeatDisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SeatDisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_seat_display, container, false);

        // Rotate Formation Left Button
        final Button leftButton = (Button) view.findViewById(R.id.buttonLeft);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success;
                if(seatFormation != null) {
                    success = formationIndexLeft();
                    if (success) {
                        displayFormation(seatFormation, selectedFormationIndex, view);
                        touchSelection = null;
                        Button reserveButton = (Button) view.findViewById(R.id.buttonReserve);
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
        final Button rightButton = (Button) view.findViewById(R.id.buttonRight);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success;
                if(seatFormation != null) {
                    success = formationIndexRight();
                    if (success) {
                        displayFormation(seatFormation, selectedFormationIndex, view);
                        touchSelection = null;
                        Button reserveButton = (Button) view.findViewById(R.id.buttonReserve);
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
        final Button reserveButton = (Button) view.findViewById(R.id.buttonReserve);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    ReserveSeatsController rsc = ReserveSeatsController.getInstance(activity.getApplicationContext());
                    if ((seatFormation != null) || (touchSelection != null)) {
                        if (touchSelection != null) {
                            selectedFormation = touchSelection.toArray(new Seat[touchSelection.size()]);
                        } else if (seatFormation != null) {
                            selectedFormation = seatFormation[selectedFormationIndex];
                            selectedFormation = stripUnavailableFromFormation(seatFormation[selectedFormationIndex]);
                        }
//                        if (pickerGroupSize.getValue() == selectedFormation.length) {
                        if (groupSize == selectedFormation.length) {
                            reserveColor = rsc.reserveSeats(selectedFormation, seatActivity);

                            if (!(reserveColor[0] == 0 && reserveColor[1] == 0 && reserveColor[2] == 0)) {
                                //Show reservation seat color with a confirmation button
                                //Clear the display to available seats again
                                reservedDialog(reserveColor[0], reserveColor[1], reserveColor[2]);
                            } else {
                                //Tell the user that their seats could not be reserved and refresh display back to available seats
                                errorRefreshDialog("Your seats have been taken, please try again.", view);
                            }
                        } else {
                            errorDialog("Please select the same amount of seats as your group size.");
                        }
                    } else {
                        errorDialog("Please select a formation or select a seat.");
                    }
                }
            }
        });
        reserveButton.setEnabled(false);

        availableSeats = DBController.getController().getAvailableSeatsInt();

        tempLinLayout = displaySeats(availableSeats, view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMakeReservation();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onMakeReservation();
    }


    /**
     * Pops up an error dialog using the listen sound
     * @param message error message
     */
    public void errorDialog(String message) {
        Activity activity = getActivity();
        if (activity != null) {
            MediaPlayer mediaPlayer = MediaPlayer.create(activity, R.raw.listen);

            final Dialog dialog = new Dialog(activity);
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
    }



    /**
     * Pops up an error dialog using the listen sound, soft refreshes after tapping confirm
     * @param message error message
     */
    public void errorRefreshDialog(String message, View view) {
        Activity activity = getActivity();
        if (activity != null) {
            MediaPlayer mediaPlayer = MediaPlayer.create(activity, R.raw.listen);

            final Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.confirm_dialog);
            dialog.setTitle(ERROR_TITLE);
            TextView textView = (TextView) dialog.findViewById(R.id.dialogText);
            textView.setText(message);

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    seatUpdateRefresh(view);
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
    }

    /**
     * Pops a message that the seat has been reserved with a look sound, hard refreshes after tapping okay
     * @param r red
     * @param g green
     * @param b blue
     */
    public void reservedDialog(int r, int g, int b) {
        Activity activity = getActivity();
        if (activity != null) {
            MediaPlayer mediaPlayer = MediaPlayer.create(activity, R.raw.look);

            final Dialog dialog = new Dialog(activity);
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
//                    hardRefresh();
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
    }


    /**
     * Refreshes to update freed up available seats without losing hand selected formations
     */
    public void seatUpdateRefresh(View view) {
        availableSeats = DBController.getController().getAvailableSeatsInt();
        if((seatFormation == null) && (touchSelection == null)){
            tempLinLayout = displaySeats(availableSeats, view);
        } else {
            if(touchSelection != null){
                tempLinLayout = displaySelection(touchSelection, view);
            }
            else if(seatFormation != null) {
                tempLinLayout = displayFormation(seatFormation, selectedFormationIndex, view);
            }
        }
    }

    /**
     * Adds one row to layoutName (Adds a horizontal LinearLayout).
     * @param layoutName linear layout to add row to
     * @param rownumber the next row number of the linear layout
     * @return LinearLayout
     */
    public LinearLayout addLayoutRow(LinearLayout layoutName, int rownumber) {
        Activity activity = getActivity();
        if(activity != null) {
            LinearLayout layoutRow = new LinearLayout(activity.getApplicationContext());
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
        } else{
            return null;
        }
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
    public void imageSelection(SeatImageView imageSeat, View view) {
        Activity activity = getActivity();
        if (activity != null) {
            Drawable d = getResources().getDrawable(R.drawable.unavailable_seat);
            ReserveSeatsController rsv = ReserveSeatsController.getInstance(activity.getApplicationContext());
            d.setColorFilter(rsv.getCurrentPossibleColors2(), PorterDuff.Mode.MULTIPLY);
            NumberPicker pickerGroupSize = (NumberPicker) view.findViewById(R.id.pickerGroupSize);
            int groupSize = pickerGroupSize.getValue();
            Button reserveButton = (Button) view.findViewById(R.id.buttonReserve);

            if (imageSeat.isAvailable() && !imageSeat.isReserved()) {
                if (touchSelection == null) {
                    if (seatFormation == null) {
                        touchSelection = new ArrayList<Seat>();
                    } else {
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
                } else if (touchSelection.size() == 1) {
                    if (touchSelection.contains(imageSeat.getSeat())) {
                        touchSelection.remove(imageSeat.getSeat());
                        imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.available_seat));

                    } else {
                        touchSelection.clear();
                        touchSelection.add(imageSeat.getSeat());
                        imageSeat.setImageDrawable(getResources().getDrawable(R.drawable.select_seat));
                        imageSeat.setImageDrawable(d);
                        seatUpdateRefresh(view);
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
                if (touchSelection.size() != groupSize) {
                    reserveButton.setEnabled(false);
                } else {
                    reserveButton.setEnabled(true);
                }
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
    public SeatImageView generateSeatStatusImage(Seat seat, final View view){
        final Activity activity = getActivity();
        if (activity != null) {
            final SeatImageView imageSeat = new SeatImageView(activity.getApplicationContext(), seat);


            imageSeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Drawable d = getResources().getDrawable(R.drawable.unavailable_seat);
                    ReserveSeatsController rsv = ReserveSeatsController.getInstance(activity.getApplicationContext());
                    d.setColorFilter(rsv.getCurrentPossibleColors2(), PorterDuff.Mode.MULTIPLY);
                    if (seatFormation == null) {
                        imageSelection(imageSeat, view);
                    } else { //if a formation is already selected
                        if (touchSelection == null) {
//                        touchSelection = new ArrayList<Seat>(Arrays.asList(seatFormation[selectedFormationIndex]));
//                        touchSelection = new ArrayList<Seat>(Arrays.asList(stripUnavailableFromFormation(seatFormation[selectedFormationIndex])));
                        }
                        imageSelection(imageSeat, view);
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
        } else {
            return null;
        }
    }

    /**
     * Generates a seat image based off the information of a single seat. Takes the information of
     * whether the seat is the one selected in the formation
     * @param seat a seat
     * @return ImageView
     */
    public SeatImageView generateSeatSelectedImage(Seat seat, final View view) {
        Activity activity = getActivity();
        if (activity != null) {
            final SeatImageView imageSeat = new SeatImageView(activity.getApplicationContext(), seat);
            final Drawable d = getResources().getDrawable(R.drawable.unavailable_seat);
            ReserveSeatsController rsv = ReserveSeatsController.getInstance(activity.getApplicationContext());
            d.setColorFilter(rsv.getCurrentPossibleColors2(), PorterDuff.Mode.MULTIPLY);

            imageSeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (touchSelection == null) {
                        touchSelection = new ArrayList<Seat>(Arrays.asList(seatFormation[selectedFormationIndex]));
                    }
                    imageSelection(imageSeat,view);
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
        return null;
    }

    /**
     *  Displays current seats based off the multidimensional array of seats information.
     * @param seats available seats
     * @return linear layout of seats
     */
    public LinearLayout[] displaySeats(int[][] seats, View view) {
        Activity activity = getActivity();
        if (activity != null) {
            LinearLayout layoutRows = (LinearLayout) view.findViewById(R.id.layoutRows);
            LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
            layoutRows.removeAllViews();
            ReserveSeatsController rsv = ReserveSeatsController.getInstance(activity.getApplicationContext());

            Seat available;
            Seat unavailable;

            for (int row = 0; row < MAX_ROW; row++) {
                tempLayout[row] = addLayoutRow(layoutRows, row);
                for (int column = 0; column < MAX_COLUMN; column++) {
                    if (seats[row][column] == 1) {
                        available = new Seat(true, rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(available,view), tempLayout[row]);
                    } else {
                        unavailable = new Seat(false, rsv.getCurrentPossibleColors(), row, column);
                        addSeatImage(generateSeatStatusImage(unavailable,view), tempLayout[row]);
                    }

                }
            }
            return tempLayout;
        } else{
            return null;
        }
    }


    /**
     * Displays one formation from a given index. Generates each seat and checks each seat in the formation
     * to see if it matches the row and column of the current seat
     * @param formation The seat formations
     * @param formIndex The formation number
     * @return a linear layout of the formation
     */
    public LinearLayout[] displayFormation(Seat[][] formation, int formIndex, View view) {
        LinearLayout layoutRows = (LinearLayout) view.findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        int r;
        int c;
        Seat unavailable;
        Seat available;
        boolean seatMade;

        Activity activity = getActivity();
        if (activity != null) {
            ReserveSeatsController rsv = ReserveSeatsController.getInstance(activity.getApplicationContext());

            for (int row = 0; row < MAX_ROW; row++) { //create a row
                tempLayout[row] = addLayoutRow(layoutRows, row);
                for (int column = 0; column < MAX_COLUMN; column++) { //create a new column
                    seatMade = false;
                    for (int seatInFormation = 0; seatInFormation < formation[formIndex].length; seatInFormation++) {
                        //go through one formation based off the formIndex to find a seat matching the current iteration of row and column
                        if (formation[formIndex][seatInFormation] != null) { // If there's an actual seat, temporarily save the row and column
                            r = formation[formIndex][seatInFormation].getRow();
                            c = formation[formIndex][seatInFormation].getCol();
                            if ((r == row) && (c == column)) { // If the row and column is matches this iteration of row and columns
                                //If this seat is in the seat formation, generate a view that indicates it's part of the formation
                                if (formation[formIndex][seatInFormation].isAvailable() && !formation[formIndex][seatInFormation].getReserved()) {
                                    addSeatImage(generateSeatSelectedImage(formation[formIndex][seatInFormation], view), tempLayout[row]);
                                    seatMade = true;
                                }

                            }
                        }
                    }
                    // If this seat is not in the seat formation, check if it is one of the available seats
                    // and indicate whether it's an available seat or not
                    if (seatMade == false) {
                        if (availableSeats[row][column] == 1) {
                            available = new Seat(true, rsv.getCurrentPossibleColors(), row, column);
                            addSeatImage(generateSeatStatusImage(available, view), tempLayout[row]);
                        } else {
                            unavailable = new Seat(false, rsv.getCurrentPossibleColors(), row, column);
                            addSeatImage(generateSeatStatusImage(unavailable, view), tempLayout[row]);
                        }
                    }
                }
            }
            return tempLayout;
        } else{
            return null;
        }
    }


    /**
     * Displays one formation from a given index. Generates each seat and checks each seat in the formation
     * to see if it matches the row and column of the current seat
     * @param formation The seat formations
     * @return a linear layout of the formation
     */
    public LinearLayout[] displaySelection(ArrayList<Seat> formation, View view) {
        LinearLayout layoutRows = (LinearLayout) view.findViewById(R.id.layoutRows);
        LinearLayout[] tempLayout = new LinearLayout[MAX_ROW];
        layoutRows.removeAllViews();
        int r;
        int c;
        Seat unavailable;
        Seat available;
        boolean seatMade;
        Activity activity = getActivity();
        if (activity != null) {
            ReserveSeatsController rsv = ReserveSeatsController.getInstance(activity.getApplicationContext());

            for (int row = 0; row < MAX_ROW; row++) { //create a row
                tempLayout[row] = addLayoutRow(layoutRows, row);
                for (int column = 0; column < MAX_COLUMN; column++) { //create a new column
                    seatMade = false;
                    for (int seatInFormation = 0; seatInFormation < formation.size(); seatInFormation++) {
                        //go through one formation based off the formIndex to find a seat matching the current iteration of row and column
                        if (formation.get(seatInFormation) != null) { // If there's an actual seat, temporarily save the row and column
                            r = formation.get(seatInFormation).getRow();
                            c = formation.get(seatInFormation).getCol();
                            if ((r == row) && (c == column)) { // If the row and column is matches this iteration of row and columns
                                //If this seat is in the seat formation, generate a view that indicates it's part of the formation
                                if (formation.get(seatInFormation).isAvailable() && !formation.get(seatInFormation).getReserved()) {
                                    addSeatImage(generateSeatSelectedImage(formation.get(seatInFormation), view), tempLayout[row]);
                                    seatMade = true;
                                }

                            }
                        }
                    }
                    // If this seat is not in the seat formation, check if it is one of the available seats
                    // and indicate whether it's an available seat or not
                    if (seatMade == false) {
                        if (availableSeats[row][column] == 1) {
                            available = new Seat(true, rsv.getCurrentPossibleColors(), row, column);
                            addSeatImage(generateSeatStatusImage(available, view), tempLayout[row]);
                        } else {
                            unavailable = new Seat(false, rsv.getCurrentPossibleColors(), row, column);
                            addSeatImage(generateSeatStatusImage(unavailable, view), tempLayout[row]);
                        }
                    }
                }
            }
            return tempLayout;
        } else{
            return null;
        }
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



}
