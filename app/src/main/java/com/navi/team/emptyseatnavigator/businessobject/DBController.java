package com.navi.team.emptyseatnavigator.businessobject;

import com.navi.team.emptyseatnavigator.activities.SeatActivity;

import java.util.ArrayList;

/**
 * Created by oguni on 4/3/2015.
 */
public class DBController implements Constants{
    private Seat seats[][];
    private static int objCount =0;
    private static DBController controller = null;

    private DBController(){
        seats = new Seat[MAX_ROW][MAX_COLUMN];
        seatAllAvailable();
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
                if (seats[row][col].isAvailable() && !seats[row][col].getReserved()){
                    results[row][col] = 1;
                }
                else {
                    results[row][col] =0;
                }
            }
        }
        return results;
    }


    public boolean updateSeat(Seat newSeat){
        int row = newSeat.getRow();
        int col = newSeat.getCol();

        Seat currentSeat = seats[row][col];
        currentSeat.setAvailable(newSeat.isAvailable());
        currentSeat.setReserved(newSeat.getReserved());
        currentSeat.setColor(newSeat.getColor());
        return true;
    }

    public Seat getSeatStatus(int row, int col){
        return seats[row][col];
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
            if (seats[row][col].isAvailable() && !seats[row][col].getReserved()){
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
