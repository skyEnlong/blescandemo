package com.communication.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by workEnlong on 2015/2/25.
 */
public class CodoonHealthDevice implements Serializable, Comparable{
    public String device_type_name;
    public String id;
    public String address;
    public int rssi;
    public List<String> uuids = new ArrayList();
    public boolean isBle;
    public boolean isAutoSync;
    public boolean isRomBand;
    public boolean iscanFriend;
    public int function_type;
    public byte[] manufacturer;

    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        if(null == o) return false;
        if(o instanceof CodoonHealthDevice){
            CodoonHealthDevice toCompare = (CodoonHealthDevice) o;
            return address.equals(toCompare.address);
        }
        return false;
    }


    @Override
    public int compareTo(Object another) {
        if(null != another &&
                (another instanceof CodoonHealthDevice) ){
            CodoonHealthDevice com = (CodoonHealthDevice) another;

            return  com.rssi - this.rssi;
        }
        return 1;
    }
}
