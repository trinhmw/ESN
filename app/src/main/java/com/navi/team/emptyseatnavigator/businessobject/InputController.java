package com.navi.team.emptyseatnavigator.businessobject;

import com.navi.team.emptyseatnavigator.R;

/**
 * Created by Melissa on 3/22/2015.
 */
public class InputController {

    public boolean validateInput(int groupSize, String seatPreference, int MAX_GROUP_SIZE){
        boolean isValid = true;
        if(seatPreference == null){
            isValid = false;
        }
        else{
            if((groupSize < 1) || (groupSize > MAX_GROUP_SIZE)){
                isValid = false;
            }
            if((!seatPreference.equals(R.string.pref_front)) || (!seatPreference.equals(R.string.pref_middle)) || (!seatPreference.equals(R.string.pref_back) || (!seatPreference.equals("None")))){
                isValid = false;
            }
            if(MAX_GROUP_SIZE < 1){
                isValid = false;
            }
            if(isValid == true){
//            Call SeatingAlgorithm.algorithmstart() or something along those lines
            }

        }
        return isValid;
    }

}
