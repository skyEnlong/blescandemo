package com.communication.bean;

/**
 * Created by workEnlong on 2015/12/4.
 */
public class GpsBleFileBean {
    public String flag;
    public String name;
    public int size;
    public String address = "";


    @Override
    public boolean equals(Object o) {
        if(o instanceof  GpsBleFileBean){
            GpsBleFileBean d = (GpsBleFileBean) o;
            if(this.flag.equals(d.flag) &&
                    this.name.equals(d.name)
                    && this.address.equals(d.address)){
                return true;
            }
        }
        return super.equals(o);
    }
}
