package com.navi.team.emptyseatnavigator.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.navi.team.emptyseatnavigator.R;
import com.navi.team.emptyseatnavigator.businessobject.Constants;
import com.navi.team.emptyseatnavigator.businessobject.InputController;
import com.navi.team.emptyseatnavigator.businessobject.Seat;


public class PreferenceFragment extends Fragment implements Constants{
    private int[][] availableSeats = new int[MAX_ROW][MAX_COLUMN];
    private Seat[][] seatFormation;
    private final String ERROR_TITLE = "Hey Listen!";

    private OnFragmentInteractionListener mListener;

    public PreferenceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);

        Bundle bundle = this.getArguments();
        if(bundle != null){
            availableSeats = (int[][])bundle.getSerializable("availableSeats");
        }

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        final View view = localInflater.inflate(R.layout.fragment_preference, container, false);

        // Group size picker
        final NumberPicker pickerGroupSize = (NumberPicker) view.findViewById(R.id.pickerGroupSize);
        pickerGroupSize.setMaxValue(MAX_GROUP_SIZE);
        pickerGroupSize.setMinValue(1);
        pickerGroupSize.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


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
                        errorDialog("There are not enough seats available for your group.");
                    }
                    else {
                        seatFormation = null;
                        seatFormation = InputController.validateInput(pickerGroupSize.getValue(), preference.getText().toString(), MAX_GROUP_SIZE);
                        if (seatFormation != null) {
                            mListener.onSubmit(seatFormation, pickerGroupSize.getValue());
                        } else {
                            errorDialog("No seat formations available.");
                        }
                    }
                }
            }
        });
        return view;
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

    public interface OnFragmentInteractionListener {
        public void onSubmit(Seat[][] seatFormation, int groupSize);
    }

    /**
     * Pops up an error dialog using the listen sound
     * @param message error message
     */
    public void errorDialog(String message) {
        Activity activity = getActivity();
        if (activity != null) {
            final Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.confirm_dialog);
            dialog.setTitle(ERROR_TITLE);
            TextView textView = (TextView) dialog.findViewById(R.id.dialogText);
            textView.setText(message);
            dialog.setCanceledOnTouchOutside(false);

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
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
