package com.navi.team.emptyseatnavigator.Test;

import com.navi.team.emptyseatnavigator.businessobject.Seat;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SeatTest {
    Seat seat = new Seat();

    @Before
    public void setUp() throws Exception {
        int[] color = {40,20,30};
        seat.setColor(color);

    }

    @Test
    public void testGetR() throws Exception {
        int expected = 40;
        int actual = seat.getR();
        assertEquals(expected,actual);

    }

    @Test
    public void testGetG() throws Exception {
        int expected = 20;
        int actual = seat.getG();
        assertEquals(expected,actual);
    }

    @Test
    public void testGetB() throws Exception {
        int expected = 30;
        int actual = seat.getB();
        assertEquals(expected,actual);
    }

    @Test
    public void testSetColor1() throws Exception {
        int r = -1;
        int g = 0;
        int b = 0;
        boolean expected = false;
        boolean actual = seat.setColor(r,g,b);
        assertEquals(expected,actual);
    }

    @Test
    public void testSetColor2() throws Exception {
        int r = 0;
        int g = -1;
        int b = 255;
        boolean expected = false;
        boolean actual = seat.setColor(r,g,b);
        assertEquals(expected,actual);
    }

    @Test
    public void testSetColor3() throws Exception {
        int r = 255;
        int g = 255;
        int b = -1;
        boolean expected = false;
        boolean actual = seat.setColor(r,g,b);
        assertEquals(expected,actual);
    }

    @Test
    public void testSetColor4() throws Exception {
        int r = 255;
        int g = 255;
        int b = 256;
        boolean expected = false;
        boolean actual = seat.setColor(r,g,b);
        assertEquals(expected,actual);
    }

    @Test
    public void testSetColor5() throws Exception {
        int r = 255;
        int g = 256;
        int b = 255;
        boolean expected = false;
        boolean actual = seat.setColor(r,g,b);
        assertEquals(expected,actual);
    }

    @Test
    public void testSetColor6() throws Exception {
        int r = 256;
        int g = 255;
        int b = 255;
        boolean expected = false;
        boolean actual = seat.setColor(r,g,b);
        assertEquals(expected,actual);
    }

    @Test
    public void testSetColor7() throws Exception {
        int r = 255;
        int g = 255;
        int b = 255;
        seat.setColor(r,g,b);
        int[] set = {255,255,255};
        int[] get = seat.getColor();
        Boolean expected = true;
        Boolean actual = true;
        for(int i = 0; i<3; i++){
            if(set[i] != get[i]){
                actual = false;
            }
        }
        assertEquals(expected,actual);
    }

}