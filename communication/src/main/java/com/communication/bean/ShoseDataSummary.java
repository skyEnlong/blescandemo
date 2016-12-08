package com.communication.bean;

import java.io.Serializable;

/**
 * Created by workEnlong on 2016/1/21.
 */
public class ShoseDataSummary implements Serializable{
//    "total_distance" : 1,    //int, 单位：米
//            "walk_distance" : 1,    //int, 单位：米
//            "total_duration" : 1,    //int, 单位：秒
//            "device_id" : "",    //string
//            "day_string" : "2016-01-01",   //string
//            "walk_steps" : 1,      //int
//            "walk_duration" : 1,  //int, 单位：秒
//            "run_steps" : 1,       //int
//            "run_distance" : 1,   //int, 单位：米
//            "run_duration" : 1,   //int, 单位：秒
//            "total_steps" : 1,       //int

    public int total_distance;
    public int walk_distance;

    public int total_duration;

    public String day_string;

    public String device_id;

    public int walk_steps;

    public int walk_duration;

    public int run_steps;

    public int run_distance;

    public int run_duration;

    public int total_steps;

    @Override
    public String toString() {
        return "{" +
                "total_distance=" + total_distance +
                ", walk_distance=" + walk_distance +
                ", total_duration=" + total_duration +
                ", day_string='" + day_string + '\'' +
                ", device_id='" + device_id + '\'' +
                ", walk_steps=" + walk_steps +
                ", walk_duration=" + walk_duration +
                ", run_steps=" + run_steps +
                ", run_distance=" + run_distance +
                ", run_duration=" + run_duration +
                ", total_steps=" + total_steps +
                '}';
    }
}
