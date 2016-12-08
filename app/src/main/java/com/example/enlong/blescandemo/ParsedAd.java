package com.example.enlong.blescandemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by enlong on 16/9/27.
 */
public class ParsedAd {
    public byte flags;
    public List uuids = new ArrayList();
    public String localName;
    public byte[] manufacturer;
    public String address;

    @Override
    public String toString() {
        return "ParsedAd{" +
                "flags=" + flags +
                ", uuids=" + uuids +
                ", localName='" + localName + '\'' +
                ", manufacturer=" + Arrays.toString(manufacturer) +
                '}';
    }
}
