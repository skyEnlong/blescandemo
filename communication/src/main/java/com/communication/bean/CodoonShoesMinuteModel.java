package com.communication.bean;

import java.io.Serializable;

/**
 * Created by enlong on 2016/12/7.
 */

public class CodoonShoesMinuteModel implements Serializable {
    /**
     * 步数
     **/
    public int step;


    /**
     * 距离
     **/
    public float distance;


    /**
     * 前脚掌着地步数
     **/
    public int frontOnStep;


    /**
     * 后脚跟着地步数
     **/
    public int backOnStep;


    /**
     * 足内翻步数
     **/
    public int outFootCount;


    /**
     * 足外翻步数
     **/
    public int inFootCount;



    /**
     * 缓冲击力
     **/
    public float cachPower;

    @Override
    public String toString() {
        return "{" +
                "step=" + step +
                ", front=" + frontOnStep +
                ", back=" + backOnStep +
                ", out=" + outFootCount +
                ", in=" + inFootCount +
                ", cp=" + cachPower +
                '}';
    }
}
