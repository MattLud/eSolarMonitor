package com.solartrackr.egauge.widget.util;

/**
 * Created by mludlum on 7/26/16.
 */
public class CSVTimePeriod {

    private double time;
    private double Generation;
    private double Usage;

    public CSVTimePeriod()
    {

    }

    public CSVTimePeriod(double time, double usage,double generation) {
        this.time = time;
        Generation = generation;
        Usage = usage;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getGeneration() {
        return Generation;
    }

    public void setGeneration(double generation) {
        Generation = generation;
    }

    public double getUsage() {
        return Usage;
    }

    public void setUsage(double usage) {
        Usage = usage;
    }
}
