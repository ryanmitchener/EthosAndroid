package com.ethossoftworks.uitest;


import com.ethossoftworks.ethos.Util.Bit;

import org.junit.Test;

import static org.junit.Assert.*;

public class BitTest {
    @Test
    public void setTest() throws Exception {
        assertEquals(2345746, Bit.set(2345234, 9));
    }


    @Test
    public void unsetTest() throws Exception {
        assertEquals(2345234, Bit.unset(2345746, 9));
    }


    @Test
    public void toggleTest() throws Exception {
        assertEquals(10, Bit.toggle(8, 1));
    }


    @Test
    public void addNoMaxTest() throws Exception {
        assertEquals(804, Bit.add(100, 4));
    }


    @Test
    public void addWithMaxTest() throws Exception {
        assertEquals(12870, Bit.add(100, 70, 100));
    }


    @Test
    public void readTest() throws Exception {
        assertEquals(70, Bit.read(12870, 0, 7));
    }


    @Test
    public void readWithMaxTest() throws Exception {
        assertEquals(75, Bit.readWithMaxValue(9665, 7, 100));
    }
}