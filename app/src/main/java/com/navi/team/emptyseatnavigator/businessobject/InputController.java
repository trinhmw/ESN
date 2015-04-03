package com.navi.team.emptyseatnavigator.businessobject;

public class InputController {

    public static Seat[][] validateInput(int groupSize, String seatPreference, int MAX_GROUP_SIZE){
        boolean isValid = true;
        Seat[][] formations = null;
        if(seatPreference == null){
            isValid = false;
        }
        else{
            if((groupSize < 1) || (groupSize > MAX_GROUP_SIZE)){
                isValid = false;
            }
            if((!seatPreference.equals("Front")) && (!seatPreference.equals("Middle")) && (!seatPreference.equals("Back")) && (!seatPreference.equals("None"))){
                isValid = false;
            }
            if(MAX_GROUP_SIZE < 1){
                isValid = false;
            }
        }

        if(isValid){
            formations = SeatingLogic.SeatingAlgorithm(groupSize, seatPreference);
        }
        return formations;
    }
}