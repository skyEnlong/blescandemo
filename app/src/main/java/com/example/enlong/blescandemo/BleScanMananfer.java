package com.example.enlong.blescandemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.communication.bean.CodoonHealthDevice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;
import java.util.UUID;

/**
 * Created by enlong on 16/9/27.
 */
public class BleScanMananfer {
    boolean isSearch = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private OnDeviceSearch ca;
    private Context mContext;

    public BleScanMananfer(Context mContext){
        this.mContext = mContext;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(Build.VERSION.SDK_INT < 18) return;
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi,
                                 byte[] scanRecord) {
                // TODO Auto-generated method stub

//                CodoonHealthDevice ads = parseData(device, scanRecord);
                ParsedAd parseDat = parseData(scanRecord);
                if(null == parseDat.localName) parseDat.localName = device.getName();
                if(null != parseDat.localName &&
                        null != parseDat.manufacturer ){
                    CodoonHealthDevice device1 = new CodoonHealthDevice();
                    device1.address = device.getAddress();
                    device1.device_type_name = parseDat.localName;
                    device1.manufacturer = parseDat.manufacturer;
                    device1.rssi = rssi;

                    parseDat.address = device.getAddress();

                    if(null != ca) ca.onDeviceSearch(device1);
                }


            }
        };
    }

    public boolean isScan(){
        return  isSearch;
    }

    public void startScan(){
        if(Build.VERSION.SDK_INT < 18) return;

        isSearch = mBluetoothAdapter.startLeScan(mLeScanCallback);
//        Log.i("cod_smart", "start scan:" + isSearch);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothDevice.ACTION_FOUND);
//        mContext.registerReceiver(mReceiver, filter);
//        isSearch = mBluetoothAdapter.startDiscovery();
    }

    public void setCallBack( OnDeviceSearch ca){
        this.ca = ca;

    }

    public void  stopScan(){
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        isSearch = false;
//        mContext.unregisterReceiver(mReceiver);
    }


    public static ParsedAd parseData(byte[] adv_data) {
        ParsedAd parsedAd = new ParsedAd();
        ByteBuffer buffer = ByteBuffer.wrap(adv_data).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0)
                break;
            byte type = buffer.get();
            length -= 1;
            switch (type) {
                case 0x01: // Flags
                    parsedAd.flags = buffer.get();
                    length--;
                    break;
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                case 0x14: // List of 16-bit Service Solicitation UUIDs
                    while (length >= 2) {
                        parsedAd.uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;
                case 0x04: // Partial list of 32 bit service UUIDs
                case 0x05: // Complete list of 32 bit service UUIDs
                    while (length >= 4) {
                        parsedAd.uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getInt())));
                        length -= 4;
                    }
                    break;
                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                case 0x15: // List of 128-bit Service Solicitation UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        parsedAd.uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;
                case 0x08: // Short local device name
                case 0x09: // Complete local device name
                    byte sb[] = new byte[length];
                    buffer.get(sb, 0, length);
                    length = 0;
                    parsedAd.localName = new String(sb).trim();
                    break;
                case (byte) 0xFF: // Manufacturer Specific Data
                    byte manufacturer[] = new byte[length];
                    buffer.get(manufacturer, 0, length);
                    parsedAd.manufacturer = manufacturer;
                    length = 0;
                    break;
                default: // skip
                    break;
            }
            if (length > 0) {
                buffer.position(buffer.position() + length);
            }
        }
        return parsedAd;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


//                if(!"COD_SMART".equals(device.getName())) return;

                Bundle bd = intent.getExtras();
                if (bd == null) {
                }
                Set<String> keys = bd.keySet();
                for (String s : keys) {
                    try {
                        com.communication.data.CLog.i("auth", "key " + s + " value:" + bd.getString(s));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {


            }
        }
    };

}
