package com.communication.bean;

/**
 * Created by workEnlong on 2015/12/8.
 */
public class GPSBandPoint implements Cloneable{
    public double lat;
    public double longti;
    public double alti;
    public int step;
    public long time;
    public int state;

    @Override
    public String toString() {
        return "{" +
                "lat=" + lat +
                ", longti=" + longti +
                ", alti=" + alti +
                ", step=" + step +
                ", time=" + time +
                ", state=" + state +
                '}';
    }

    public GPSBandPoint clone(){
        GPSBandPoint clone = null;
        try {
            clone = (GPSBandPoint)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }
}
