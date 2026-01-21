package com.michael.bluetooth;

import java.io.Serializable;
import java.util.Calendar;

public class MyDate implements Serializable
{
    private int hour;
    private int minute;

    public MyDate()
    {
        this.hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        this.minute = Calendar.getInstance().get(Calendar.MINUTE);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public String toString() {
        return hour + ":" + minute;
    }
}
