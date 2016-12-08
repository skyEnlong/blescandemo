package com.communication.util;

/**
 * Created by workEnlong on 2016/1/20.
 */
public class CodoonEncrypt {
    public static byte[] xorKey = new byte[]{0x54, (byte) (0x91 & 0xff), 0x28, 0x15,
            0x57, 0x26};

    public static byte encryptMyxor(byte original, int n){
        return encryptMyxor(original & 0xff, n);

    }


    public static byte encryptMyxor(int original, int n){
        return (byte) ((original ^ xorKey[n % xorKey.length]) & 0xFF);

    }
}
