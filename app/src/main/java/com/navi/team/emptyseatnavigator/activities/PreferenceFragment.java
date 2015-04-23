package com.navi.team.emptyseatnavigator.activities;

import android.app.Activity;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.businessobject.Constants;
import com.navi.team.emptyseatnavigator.businessobject.InputController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreferenceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreferenceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferenceFragment extends Fragment implements Constants{
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
     * @return A new instance of fragment PreferenceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PreferenceFragment newInstance(String param1, String param2) {
        PreferenceFragment fragment = new PreferenceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PreferenceFragment() {
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
        // Group size picker
        final View view = inflater.inflate(R.layout.fragment_preference, container, false);
        final NumberPicker pickerGroupSize = (NumberPicker) view.findViewById(R.id.pickerGroupSize);
        pickerGroupSize.setMaxValue(MAX_GROUP_SIZE);
        pickerGroupSize.setMinValue(1);
        pickerGroupSize.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerGroupSize.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Button reserveButton = (Button) view.findViewById(R.id.buttonReserve);
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
        final RadioGroup seatPreferences = (RadioGroup) view.findViewById(R.id.seatPreferences);
        seatPreferences.check(R.id.prefNone);

        // Submit button
        final Button submitButton = (Button) view.findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedPreference = seatPreferences.getCheckedRadioButtonId();
                RadioButton preference = (RadioButton) view.findViewById(checkedPreference);
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
//                            displayFormation(seatFormation, selectedFormationIndex);
                            Button rightButton = (Button) view.findViewById(R.id.buttonRight);
                            Button leftButton = (Button) view.findViewById(R.id.buttonLeft);
                            Button reserveButton = (Button) view.findViewById(R.id.buttonReserve);
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

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSubmit();
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
        public void onSubmit();
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
    public void errorRefreshDialog(String message) {
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
//                    seatUpdateRefresh();
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

}
