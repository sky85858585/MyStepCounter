package com.elliott85.mystepcounter.content;

/**
 * Created by 박현우 on 2016-12-04.
 */
public class StepCountData {
    private String date;
    private long count;
    private double distance;

    public StepCountData (String date, long count, double distance) {
        this.date = date;
        this.count = count;
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public long getCount() {
        return count;
    }

    public double getDistance() {
        return distance;
    }
}
