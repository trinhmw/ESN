package com.navi.team.emptyseatnavigator.businessobject;

import java.util.ArrayList;

/**
 * Created by oguni on 4/3/2015.
 */
public class DBController {
    private Seat seats[][];
    private boolean availability;
    private static int objCount =0;
    private static DBController controller = null;
    int RESERVED = 384;
    int ERROR =76;

    class Result {
        Result previous;
        Boolean success;
        int code;
        Result next;
    }

    private DBController(){
        seats = new Seat[3][4];
        availability = true;
    }

    public static DBController getController(){
        if (objCount == 0){
            objCount++;
            controller = new DBController();
            return controller;
        }
        return controller;
    }

    public int[][] getAvailableSeats(){
        int[][] results = new int[3][4];
        for (int row =0; row< 3; row++){
            for (int col =0; col<4; col++){
                if (seats[row][col].isAvailable()){
                    results[row][col] = 1;
                }
                else {
                    results[row][col] =0;
                }
            }
        }
        return results;
    }

    public boolean reserveSeats(ArrayList<int[][]> rSeats){
        boolean status = true;
        for (int x =0; x< rSeats.size(); x++){
            int seatX = rSeats.get(x)[0][0];
            int seatY = rSeats.get(x)[0][1];
            if (seats[seatX][seatY].isAvailable()){
                continue;
            } else {
                status = false;
                break;
            }
        }
        if (status){
            for (int x =0; x< rSeats.size(); x++){
                int seatX = rSeats.get(x)[0][0];
                int seatY = rSeats.get(x)[0][1];
                seats[seatX][seatY].setAvailable(false);
            }
        } else{
            return status;
        }

        for (int x =0; x< rSeats.size(); x++){
            int seatX = rSeats.get(x)[0][0];
            int seatY = rSeats.get(x)[0][1];
            //Call sendMessage with x, y, and the constant RESERVED
        }

        return status;
    }
}
