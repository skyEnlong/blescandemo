package com.example.enlong.blescandemo;

import java.io.Serializable;

/**
 * Created by enlong on 16/10/19.
 */
public class MsgEvent implements Serializable{
    public int event_id;
    public String msg;

    public int  start;
    public int  current;
    public long  totalLost;

    public long  lost2;
    public long  lost5;
    public long  lost10;
    public long  lostMore;
}
