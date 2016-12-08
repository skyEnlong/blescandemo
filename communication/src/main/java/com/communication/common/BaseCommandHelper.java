package com.communication.common;

/**
 * Created by workEnlong on 2016/1/20.
 */
public class BaseCommandHelper {
    public  byte[] getCommand(int command, byte[] data){
        int value_length = 0;
        int check = 0;
        if(null != data ){
            value_length = data.length;
        }

        byte[] bytes = new byte[1 + 2 + value_length + 1];

        bytes[0] = (byte)(0xAA & 0xff);
        bytes[1] = (byte)(command & 0xff);
        bytes[2] = (byte)(value_length & 0xff);
        if(value_length >0){
            for (int i = 0; i < value_length; i++){
                bytes[3 + i] = data[i];
            }
        }

        for(byte v : bytes){
            check += v & 0xff;
        }

        bytes[bytes.length - 1] = (byte) (check & 0xff);

        return bytes;
    }

    public  byte[] getCommand(int command){
        return getCommand(command, null);
    }

}
