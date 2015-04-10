package com.navi.team.emptyseatnavigator.businessobject;

import com.navi.team.emptyseatnavigator.activities.SeatActivity;

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
    private final int MAX_COLUMN = 4;
    private final int MAX_ROW = 3;

    class Result {
        Result previous;
        Boolean success;
        int code;
        Result next;
    }

    private DBController(){
        seats = new Seat[MAX_ROW][MAX_COLUMN];
        seatAllAvailable();
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

    public int[][] getAvailableSeatsInt(){
        int[][] results = new int[MAX_ROW][MAX_COLUMN];
        for (int row =0; row< MAX_ROW; row++){
            for (int col =0; col<MAX_COLUMN; col++){
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

    public Seat[][] getAvailableSeats(){
        Seat[][] results = new Seat[MAX_ROW][MAX_COLUMN];
        for (int row =0; row< MAX_ROW; row++){
            for (int col =0; col<MAX_COLUMN; col++){
                if (seats[row][col].isAvailable()){
                    results[row][col] = seats[row][col];
                }
                else{
                    results[row][col] = null;
                }

            }
        }
        return results;
    }

    public boolean reserveSeats(ArrayList<int[][]> rSeats, SeatActivity act){
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
            act.sendMessage(seats[seatX][seatY]);
        }

        return status;
    }

    public boolean updateSeat(Seat newSeat){
        int row = newSeat.getRow();
        int col = newSeat.getCol();

        Seat currentSeat = seats[row][col];
        currentSeat.setAvailable(newSeat.isAvailable());
        currentSeat.setColor(newSeat.getColor());
        return true;
    }


    /**
     * Reserves the seats given an array of seats to be reserved.
     * @param rSeats seats to be reserved
     * @return boolean true for successfully reserved, false for reservation failure
     */
    public boolean reserveSeats(Seat[] rSeats, SeatActivity act){
        boolean status = true;
//      Check to see if all seats are available for reservation
        for (int x = 0; x < rSeats.length; x++){
            int row = rSeats[x].getRow();
            int col = rSeats[x].getCol();
            if (seats[row][col].isAvailable()){
                continue;
            } else {
                status = false;
                break;
            }
        }
//      If the seats are all available, update map and reserve
        if (status) {
            for (int x = 0; x < rSeats.length; x++) {
                int row = rSeats[x].getRow();
                int col = rSeats[x].getCol();
                seats[row][col] = rSeats[x];
            }

            for (int x = 0; x < rSeats.length; x++) {
                int row = rSeats[x].getRow();
                int col = rSeats[x].getCol();
                act.sendMessage(seats[row][col]);
            }
        }
        return status;
    }

    /**
     * Testing purposes only
     */
    public void seatAllAvailable(){
        for(int r = 0; r < MAX_ROW; r++){
            for(int c = 0; c < MAX_COLUMN; c++){
                if(seats[r][c] == null){
                    Seat seat = new Seat(true);
                    seat.setCol(c);
                    seat.setRow(r);
                    seats[r][c] = seat;
                }
            }
        }
    }

    public Seat[][] getSeats() {
        return seats;
    }
}
