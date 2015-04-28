/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.navi.team.emptyseatnavigator.Test.Instrumentation;

import java.util.ArrayList;
import com.navi.team.emptyseatnavigator.businessobject.Seat;

/**
 *
 * @author Indon
 */
public class SeatingLogic {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Seat results[][];
        
        results = SeatingAlgorithm(1,"front");
        
        results = SeatingAlgorithm(1,"back");
        
        results = SeatingAlgorithm(1,"middle");
        
        results = SeatingAlgorithm(1,"none");
        
        results = SeatingAlgorithm(2,"middle");
        
        results = SeatingAlgorithm(3,"middle");
        
        results = SeatingAlgorithm(4,"middle");
        
    }



/**
 * Created by Indon on 3/18/2015.
 * Contains one public static method, called, creatively, SeatingAlgorithm.
 * Also contains a private method that generates a set of seats to test the algorithm with.
 */

    /**
     * Takes a number of seats from 1-4 inclusive and a string, returns a two-dimensional array of seats.
     * Each 'row' of Seats represents a set of seats returned by the algorithm.
     * @param seat_number integer, from 1-4.
     * @param preference string, either "front", "back", "middle", or "none".
     * @return Seat[][]
     */
    public static Seat[][] SeatingAlgorithm(int seat_number, String preference){

        //ArrayList<ArrayList<Seat>> returnval = new ArrayList<ArrayList<Seat>>();
        Seat [][] returnval = new Seat [1][1];

        //This will be replaced with a call to AvailableSeats.
        Seat[][] seats = GenerateFakeData();

        //if seat_number = 1, then there's one configuration: first empty seat.
        //if it's 2, we'll use three configs: horizontal adjacent, vertical adjacent, and first empty seats.
        //if it's 3, again three configs: all in a row, two in one row and one in the other, and first empty seats.
        //if it's four, then four configs: all in a row, three in one row and one in the other, two and two, and first empty seats.
        if (seat_number==1){
            returnval = new Seat [1][1];
            //temp = new Seat[1];
        }
        else if (seat_number==2){
            returnval = new Seat [3][2];
            //temp = new Seat[2];
        }
        else if (seat_number==3){
            returnval = new Seat [3][3];
            //temp = new Seat[3];
        }
        else if (seat_number==4){
            returnval = new Seat [4][4];
            //temp = new Seat[4];
        }

        //Seat preference dictates what direction iteration loops go in.
        if (preference.equals("front") | preference.equals("none")){
            //How many seats the user's looking for dictates how many configurations we look for, and what exactly each is.
            switch(seat_number){
                case 1:
                    //Don't believe java. These for loops are _not_ safely replacable with foreach.
                    for(int i=0;i<seats.length;i++){
                        for(int j=0;j<seats[i].length;j++){
                            //If the seat is null, we haven't populated it yet.
                            if(returnval[0][0]==null){
                                if(seats[i][j].isAvailable()){
                                    returnval[0][0]=seats[i][j];
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    for(int i=0;i<seats.length;i++){
                        for(int j=0;j<seats[i].length;j++){
                            //Two consecutive seats. For each of these we check one seat first then each other seat in the configuration.
                            //If all are available we fill the configuration and never check it again.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                    }
                                }
                            }
                            //Two vertically adjacent seats.
                            if(returnval[1][0]==null){
                                if((i<(seats.length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i+1][j].isAvailable()){
                                        returnval[1][0]=seats[i][j];
                                        returnval[1][1]=seats[i+1][j];
                                    }
                                }
                            }
                            //First available seats. This k-loop fills up the first empty seat in
                            // the empty seats row if the seat being iterated through is available.
                            for(int k=0;k<returnval[1].length;k++){
                                if(returnval[2][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[2][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 3:
                    for(int i=0;i<seats.length;i++){
                        for(int j=0;j<seats[i].length;j++){
                            //Three consecutive seats.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-2)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                        returnval[0][2]=seats[i][j+2];
                                    }
                                }
                            }
                            //Two seats on primary row, one seat on secondary row
                            // (next row back for front pref, next row forward for back pref)
                            if(returnval[1][0]==null){
                                if((i<(seats.length-1)) && (j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        if(seats[i+1][j].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i+1][j];
                                        }
                                        else if(seats[i+1][j+1].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i+1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[2].length;k++){
                                if(returnval[2][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[2][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 4:
                    for(int i=0;i<seats.length;i++){
                        for(int j=0;j<seats[i].length;j++){
                            //Four consecutive seats.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-3)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                        returnval[0][2]=seats[i][j+2];
                                        returnval[0][3]=seats[i][j+3];
                                    }
                                }
                            }
                            //Three in one row, one in another.
                            if(returnval[1][0]==null){
                                if((i<(seats.length-1)) && (j<(seats[i].length-2)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        if(seats[i+1][j].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i+1][j];
                                        }
                                        if(seats[i+1][j+1].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i+1][j+1];
                                        }
                                        if(seats[i+1][j+2].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i+1][j+2];
                                        }
                                    }
                                }
                            }
                            if(returnval[2][0]==null){
                                if((i<(seats.length-1)) && (j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        if(seats[i+1][j].isAvailable() && seats[i+1][j+1].isAvailable()){
                                            returnval[2][0]=seats[i][j];
                                            returnval[2][1]=seats[i][j+1];
                                            returnval[2][2]=seats[i+1][j];
                                            returnval[2][3]=seats[i+1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[3].length;k++){
                                if(returnval[3][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[3][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
            }


        }
        else if (preference.equals("back")){
            switch(seat_number){
                case 1:
                    for(int i=seats.length-1;i>=0;i--){
                        for(int j=0;j<seats[i].length;j++){
                            //If the seat is null, we haven't populated it yet.
                            if(returnval[0][0]==null){
                                if(seats[i][j].isAvailable()){
                                    returnval[0][0]=seats[i][j];
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    for(int i=seats.length-1;i>=0;i--){
                        for(int j=0;j<seats[i].length;j++){
                            //Two consecutive seats. For each of these we check one seat first then each other seat in the configuration.
                            //If all are available we fill the configuration and never check it again.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                    }
                                }
                            }
                            //Two vertically adjacent seats.
                            if(returnval[1][0]==null){
                                if((i>0) && seats[i][j].isAvailable()){
                                    if(seats[i-1][j].isAvailable()){
                                        returnval[1][0]=seats[i][j];
                                        returnval[1][1]=seats[i-1][j];
                                    }
                                }
                            }
                            //First available seats. This k-loop fills up the first empty seat in
                            // the empty seats row if the seat being iterated through is available.
                            for(int k=0;k<returnval[2].length;k++){
                                if(returnval[2][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[2][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 3:
                    for(int i=seats.length-1;i>=0;i--){
                        for(int j=0;j<seats[i].length;j++){
                            //Three consecutive seats.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-2)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                        returnval[0][2]=seats[i][j+2];
                                    }
                                }
                            }
                            //Two seats on primary row, one seat on secondary row
                            // (next row back for front pref, next row forward for back pref)
                            if(returnval[1][0]==null){
                                if((i>0) && (j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        if(seats[i-1][j].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i-1][j];
                                        }
                                        else if(seats[i-1][j+1].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i-1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[2].length;k++){
                                if(returnval[2][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[2][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 4:
                    for(int i=seats.length-1;i>=0;i--){
                        for(int j=0;j<seats[i].length;j++){
                            //Four consecutive seats.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-3)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                        returnval[0][2]=seats[i][j+2];
                                        returnval[0][3]=seats[i][j+3];
                                    }
                                }
                            }
                            //Three in one row, one in another.
                            if(returnval[1][0]==null){
                                if((i>0) && (j<(seats[i].length-2)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        if(seats[i-1][j].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i-1][j];
                                        }
                                        if(seats[i-1][j+1].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i-1][j+1];
                                        }
                                        if(seats[i-1][j+2].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i-1][j+2];
                                        }
                                    }
                                }
                            }
                            if(returnval[2][0]==null){
                                if((i>0) && (j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        if(seats[i-1][j].isAvailable() && seats[i-1][j+1].isAvailable()){
                                            returnval[2][0]=seats[i][j];
                                            returnval[2][1]=seats[i][j+1];
                                            returnval[2][2]=seats[i-1][j];
                                            returnval[2][3]=seats[i-1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[3].length;k++){
                                if(returnval[3][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[3][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
        }
        else if (preference.equals("middle")){
            //Here we run front-first loops from the middle, back-first loops from the middle, then reconcile them.
            int middle_row= (int) Math.floor(seats.length/2);
            switch(seat_number){
                case 1:
                    for(int i=middle_row;i<seats.length;i++){
                        //This lets us iterate forwards and backwards at once.
                        //This method will fill seats with whichever setup is closest to the middle row, biased towards the frontwards search.
                        int backi = (seats.length-i)-1;
                        for(int j=0;j<seats[i].length;j++){
                            //If the seat is null, we haven't populated it yet.
                            if(returnval[0][0]==null){
                                if(seats[i][j].isAvailable()){
                                    returnval[0][0]=seats[i][j];
                                }
                            }
                            //Back case.
                            if(returnval[0][0]==null){
                                if(seats[backi][j].isAvailable()){
                                    returnval[0][0]=seats[backi][j];
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    for(int i=middle_row;i<seats.length;i++){
                        int backi = (seats.length-i)-1;
                        for(int j=0;j<seats[i].length;j++){
                            //Two consecutive seats. For each of these we check one seat first then each other seat in the configuration.
                            //If all are available we fill the configuration and never check it again.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                    }
                                }
                            }
                            //Two vertically adjacent seats.
                            if(returnval[1][0]==null){
                                if((i<(seats.length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i+1][j].isAvailable()){
                                        returnval[1][0]=seats[i][j];
                                        returnval[1][1]=seats[i+1][j];
                                    }
                                }
                            }
                            //First available seats. This k-loop fills up the first empty seat in
                            // the empty seats row if the seat being iterated through is available.
                            for(int k=0;k<returnval[1].length;k++){
                                if(returnval[2][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[2][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                            //Back case.
                            if(returnval[0][0]==null){
                                if((j<(seats[backi].length-1)) && seats[backi][j].isAvailable()){
                                    if(seats[backi][j+1].isAvailable()){
                                        returnval[0][0]=seats[backi][j];
                                        returnval[0][1]=seats[backi][j+1];
                                    }
                                }
                            }
                            //Two vertically adjacent seats.
                            if(returnval[1][0]==null){
                                if((backi>0) && seats[backi][j].isAvailable()){
                                    if(seats[backi-1][j].isAvailable()){
                                        returnval[1][0]=seats[backi][j];
                                        returnval[1][1]=seats[backi-1][j];
                                    }
                                }
                            }
                            //First available seats. This k-loop fills up the first empty seat in
                            // the empty seats row if the seat being iterated through is available.
                            for(int k=0;k<returnval[1].length;k++){
                                if(returnval[2][k]==null || (returnval[2][k].getCol()==seats[backi][j].getCol() && returnval[2][k].getRow()==seats[backi][j].getRow())){
                                    if(seats[backi][j].isAvailable()){
                                        returnval[2][k]=seats[backi][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 3:
                    for(int i=middle_row;i<seats.length;i++){
                        int backi = (seats.length-i)-1;
                        for(int j=0;j<seats[i].length;j++){
                            //Three consecutive seats.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-2)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                        returnval[0][2]=seats[i][j+2];
                                    }
                                }
                            }
                            //Two seats on primary row, one seat on secondary row
                            // (next row back for front pref, next row forward for back pref)
                            if(returnval[1][0]==null){
                                if((i<(seats.length-1)) && (j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        if(seats[i+1][j].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i+1][j];
                                        }
                                        else if(seats[i+1][j+1].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i+1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[2].length;k++){
                                if(returnval[2][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[2][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                            //Back case.
                            if(returnval[0][0]==null){
                                if((j<(seats[backi].length-2)) && seats[backi][j].isAvailable()){
                                    if(seats[backi][j+1].isAvailable() && seats[backi][j+2].isAvailable()){
                                        returnval[0][0]=seats[backi][j];
                                        returnval[0][1]=seats[backi][j+1];
                                        returnval[0][2]=seats[backi][j+2];
                                    }
                                }
                            }
                            //Two seats on primary row, one seat on secondary row
                            // (next row back for front pref, next row forward for back pref)
                            if(returnval[1][0]==null){
                                if((backi>0) && (j<(seats[backi].length-1)) && seats[backi][j].isAvailable()){
                                    if(seats[backi][j+1].isAvailable()){
                                        if(seats[backi-1][j].isAvailable()){
                                            returnval[1][0]=seats[backi][j];
                                            returnval[1][1]=seats[backi][j+1];
                                            returnval[1][2]=seats[backi-1][j];
                                        }
                                        else if(seats[backi-1][j+1].isAvailable()){
                                            returnval[1][0]=seats[backi][j];
                                            returnval[1][1]=seats[backi][j+1];
                                            returnval[1][2]=seats[backi-1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[2].length;k++){
                                if(returnval[2][k]==null || (returnval[2][k].getCol()==seats[backi][j].getCol() && returnval[2][k].getRow()==seats[backi][j].getRow())){
                                    if(seats[backi][j].isAvailable()){
                                        returnval[2][k]=seats[backi][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 4:
                    for(int i=middle_row;i<seats.length;i++){
                        int backi = (seats.length-i)-1;
                        for(int j=0;j<seats[i].length;j++){
                            //Four consecutive seats.
                            if(returnval[0][0]==null){
                                if((j<(seats[i].length-3)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        returnval[0][0]=seats[i][j];
                                        returnval[0][1]=seats[i][j+1];
                                        returnval[0][2]=seats[i][j+2];
                                        returnval[0][3]=seats[i][j+3];
                                    }
                                }
                            }
                            //Three in one row, one in another.
                            if(returnval[1][0]==null){
                                if((i<(seats.length-1)) && (j<(seats[i].length-2)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable() && seats[i][j+2].isAvailable()){
                                        if(seats[i+1][j].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i+1][j];
                                        }
                                        if(seats[i+1][j+1].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i+1][j+1];
                                        }
                                        if(seats[i+1][j+2].isAvailable()){
                                            returnval[1][0]=seats[i][j];
                                            returnval[1][1]=seats[i][j+1];
                                            returnval[1][2]=seats[i][j+2];
                                            returnval[1][3]=seats[i+1][j+2];
                                        }
                                    }
                                }
                            }
                            if(returnval[2][0]==null){
                                if((i<(seats.length-1)) && (j<(seats[i].length-1)) && seats[i][j].isAvailable()){
                                    if(seats[i][j+1].isAvailable()){
                                        if(seats[i+1][j].isAvailable() && seats[i+1][j+1].isAvailable()){
                                            returnval[2][0]=seats[i][j];
                                            returnval[2][1]=seats[i][j+1];
                                            returnval[2][2]=seats[i+1][j];
                                            returnval[2][3]=seats[i+1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[i].length;k++){
                                if(returnval[3][k]==null){
                                    if(seats[i][j].isAvailable()){
                                        returnval[3][k]=seats[i][j];
                                        break;
                                    }
                                }
                            }
                            //Back case.
                            if(returnval[0][0]==null){
                                if((j<(seats[backi].length-3)) && seats[backi][j].isAvailable()){
                                    if(seats[backi][j+1].isAvailable() && seats[backi][j+2].isAvailable()){
                                        returnval[0][0]=seats[backi][j];
                                        returnval[0][1]=seats[backi][j+1];
                                        returnval[0][2]=seats[backi][j+2];
                                        returnval[0][3]=seats[backi][j+3];
                                    }
                                }
                            }
                            //Three in one row, one in another.
                            if(returnval[1][0]==null){
                                if((backi>0) && (j<(seats[i].length-2)) && seats[backi][j].isAvailable()){
                                    if(seats[backi][j+1].isAvailable() && seats[backi][j+2].isAvailable()){
                                        if(seats[backi-1][j].isAvailable()){
                                            returnval[1][0]=seats[backi][j];
                                            returnval[1][1]=seats[backi][j+1];
                                            returnval[1][2]=seats[backi][j+2];
                                            returnval[1][3]=seats[backi-1][j];
                                        }
                                        if(seats[backi-1][j+1].isAvailable()){
                                            returnval[1][0]=seats[backi][j];
                                            returnval[1][1]=seats[backi][j+1];
                                            returnval[1][2]=seats[backi][j+2];
                                            returnval[1][3]=seats[backi-1][j+1];
                                        }
                                        if(seats[backi-1][j+2].isAvailable()){
                                            returnval[1][0]=seats[backi][j];
                                            returnval[1][1]=seats[backi][j+1];
                                            returnval[1][2]=seats[backi][j+2];
                                            returnval[1][3]=seats[backi-1][j+2];
                                        }
                                    }
                                }
                            }
                            //Two and Two.
                            if(returnval[2][0]==null){
                                if((backi>0) && (j<(seats[backi].length-1)) && seats[backi][j].isAvailable()){
                                    if(seats[backi][j+1].isAvailable()){
                                        if(seats[backi-1][j].isAvailable() && seats[backi-1][j+1].isAvailable()){
                                            returnval[2][0]=seats[backi][j];
                                            returnval[2][1]=seats[backi][j+1];
                                            returnval[2][2]=seats[backi-1][j];
                                            returnval[2][3]=seats[backi-1][j+1];
                                        }
                                    }
                                }
                            }
                            //First available seats.
                            for(int k=0;k<returnval[3].length;k++){
                                //There's a bug here, in all of these middle row things, where a seat might be added twice (for i==backi).
                                //Can be fixed by adding, to the back case for each seat, a check if the seat has just been added.
                                if(returnval[3][k]==null || (returnval[3][k].getCol()==seats[backi][j].getCol() && returnval[3][k].getRow()==seats[backi][j].getRow())){
                                    if(seats[backi][j].isAvailable()){
                                        returnval[3][k]=seats[backi][j];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
        }

        //If the last seat of the last row of returnval is empty, make the entire last row of returnval empty.
        //returnvalreturnvalreturnvalreturnval
        if(returnval[returnval.length-1][returnval[returnval.length-1].length-1]==null){
            for(int i=0;i<returnval[returnval.length-1].length;i++){
                //It feels wrong re-nulling things.
                returnval[returnval.length-1][i]=null;
            }
        }

        //Let's copy everything over into an arraylist, because it makes appending easy.
        ArrayList<Seat[]> tempreturnval = new ArrayList<Seat[]>();
        //Build the arraylist by copying over nonnull rows.
        for(int i=0;i<returnval.length;i++){
            if(!(returnval[i][0]==null)){
                tempreturnval.add(returnval[i]);
            }
        }

        //Copy the arraylist back. For some reason Android Studio protested when I just toArray'd it.
        returnval= new Seat[tempreturnval.size()][returnval[0].length];
        for(int i=0;i<tempreturnval.size();i++){
            for(int j=0;j<tempreturnval.get(0).length;j++){
                returnval[i][j]=tempreturnval.get(i)[j];
            }
        }

        //If returnval is empty and I actually want this thing to send back a real error message... throw an exception? Maybe?
        return returnval;
    }

    //Currently returns a set of 12 (3x4) empty seats.
    private static Seat[][] GenerateFakeData(){
        Seat[][] returnval = new Seat [3][4];
        for(int i=0;i<3;i++){
            for(int j=0;j<4;j++){
                returnval[i][j]=new Seat(i,j,false);
                if(i==0 && j ==0){
                    returnval[i][j]=new Seat(i,j,true);
                }
            }
        }
        returnval[1][1]=new Seat(1,1,true);
        return returnval;
    }

}