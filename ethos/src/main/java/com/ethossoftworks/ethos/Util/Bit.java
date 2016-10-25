package com.ethossoftworks.ethos.Util;


public class Bit {
    public static int set(int data, int bitIndex) {
        return data | (1 << bitIndex);
    }


    public static long set(long data, int bitIndex) {
        return data | (1 << bitIndex);
    }


    public static int unset(int data, int bitIndex) {
        return data & ~(1 << bitIndex);
    }


    public static long unset(long data, int bitIndex) {
        return data & ~(1 << bitIndex);
    }


    public static int toggle(int data, int bitIndex) {
        return data ^ (1 << bitIndex);
    }


    public static long toggle(long data, int bitIndex) {
        return data ^ (1 << bitIndex);
    }


    public static int add(int data, int value) {
        return add(data, value, value + 1);
    }


    public static long add(long data, long value) {
        return add(data, value, value + 1);
    }


    public static int add(int data, int value, int maxValue) {
        int shift = (int) Math.ceil((Math.log(maxValue) / Math.log(2)));
        return  (data << shift) | value;
    }


    public static long add(long data, long value, long maxValue) {
        int shift = (int) Math.ceil((Math.log(maxValue) / Math.log(2)));
        return  (data << shift) | value;
    }


    public static int read(int data, int start, int count) {
        return (data >> start) & ((1 << count) - 1);
    }


    public static long read(long data, int start, int count) {
        return (data >> start) & ((1 << count) - 1);
    }


    public static int readWithMaxValue(int data, int start, int maxValue) {
        int shift = (int) Math.ceil((Math.log(maxValue) / Math.log(2)));
        return (data >> start) & ((1 << shift) - 1);
    }


    public static long readWithMaxValue(long data, int start, long maxValue) {
        int shift = (int) Math.ceil((Math.log(maxValue) / Math.log(2)));
        return (data >> start) & ((1 << shift) - 1);
    }
}