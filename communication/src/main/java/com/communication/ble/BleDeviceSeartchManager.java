package com.communication.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.communication.bean.CodoonHealthDevice;
import com.communication.data.CLog;
import com.communication.data.TimeoutCheck;
import com.communication.gpsband.GpsBandParseUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by workEnlong on 2015/2/25.
 */
@SuppressLint("NewApi")
public class BleDeviceSeartchManager {


    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private OnDeviceSeartchCallback mOnSeartchCallback;

    private static final String TAG = "accessory";

    private TimeoutCheck mTimeoutCheck;

    private int time_out = 15000;
    private boolean isScanBLEStart;

    private List<String> searchUUID;

    public BleDeviceSeartchManager(Context context) {
        mContext = context;
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi,
                                 byte[] scanRecord) {
                // TODO Auto-generated method stub
                boolean isInterrupt = false;

                if (null == device) {
                    return;
                }

                try{
                    CodoonHealthDevice healthDevice = parseData(device, scanRecord);

                    if (null == healthDevice || null == healthDevice.device_type_name) return;

                    if(null != searchUUID){
                        if (healthDevice.uuids == null || healthDevice.uuids.size() ==0) return;
                        List<String> findIDS = healthDevice.uuids;
                        boolean hasFind = false;

                        for(String id : findIDS){
                            for(String id_search: searchUUID){
                                if(id_search.contains(id)){
                                    hasFind = true;
                                    break;
                                }
                            }

                        }

                        if(!hasFind){
                            Log.e("accessory", "not find right uuid");

                            return;
                        }

                    }

                    healthDevice.rssi = rssi;
                    if (null != mOnSeartchCallback) {
                        mOnSeartchCallback.onSeartch(healthDevice, scanRecord);

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };

        final BluetoothManager bluetoothManager = (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mTimeoutCheck = new TimeoutCheck(new TimeoutCheck.ITimeoutCallback() {

            @Override
            public void onReceivedFailed() {
                // TODO Auto-generated method stub
                timeOutAction();
            }

            @Override
            public void onReSend() {
                // TODO Auto-generated method stub
                timeOutAction();
            }

            @Override
            public void onReConnect(int tryConnectIndex) {
                // TODO Auto-generated method stub
                timeOutAction();
            }

            @Override
            public void onConnectFailed(int tryConnectIndex) {
                // TODO Auto-generated method stub
                timeOutAction();
            }
        });

        mTimeoutCheck.setTryConnectCounts(1);
        mTimeoutCheck.setIsConnection(false);
        mTimeoutCheck.setTimeout(time_out);
    }

    /**
     * startSearch
     */
    public boolean startSearch() {
        searchUUID = null;
        if (mBluetoothAdapter.isEnabled() && !isScanBLEStart) {

            mTimeoutCheck.startCheckTimeout();
            isScanBLEStart = mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        return isScanBLEStart;
    }


    public boolean startSearch(UUID[] uuids) {

        return startSearch();
    }

    /**
     * stopSearch
     */
    public void stopSearch() {

        mTimeoutCheck.stopCheckTimeout();

        try {
            if (isScanBLEStart) {
                isScanBLEStart = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public boolean isSearching(){
        return isScanBLEStart;
    }


    public static CodoonHealthDevice parseData(BluetoothDevice device,byte[] adv_data) {
        CodoonHealthDevice parsedAd = new CodoonHealthDevice();
        parsedAd.isBle = true;

        ByteBuffer buffer = ByteBuffer.wrap(adv_data).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0)
                break;
            byte type = buffer.get();
            length -= 1;
            switch (type) {
                case 0x01: // Flags
                    byte flags = buffer.get();
                    length--;
                    break;
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                case 0x14: // List of 16-bit Service Solicitation UUIDs
                    while (length >= 2) {
                        parsedAd.uuids.add(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort()));
                        length -= 2;
                    }
                    break;
                case 0x04: // Partial list of 32 bit service UUIDs
                case 0x05: // Complete list of 32 bit service UUIDs
                    while (length >= 4) {
                        parsedAd.uuids.add(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getInt()));
                        length -= 4;
                    }
                    break;
                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                case 0x15: // List of 128-bit Service Solicitation UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        parsedAd.uuids.add(new UUID(msb, lsb).toString());
                        length -= 16;
                    }
                    break;
                case 0x08: // Short local device name
                case 0x09: // Complete local device name
                    byte sb[] = new byte[length];
                    buffer.get(sb, 0, length);
                    length = 0;
                    parsedAd.device_type_name = new String(sb).trim();
                    break;
                case (byte) 0xFF: // Manufacturer Specific Data
                    byte manufacturer[] = new byte[length];
                    buffer.get(manufacturer, 0, length);
                    parsedAd.manufacturer = manufacturer;
                    getDeiviceId(parsedAd);
                    length = 0;
                    break;
                default: // skip
                    break;
            }
            if (length > 0) {
                buffer.position(buffer.position() + length);
            }
        }

        if(null == parsedAd.device_type_name ) parsedAd.device_type_name = device.getName();
        parsedAd.address = device.getAddress();

        return parsedAd;
    }


    public static void getDeiviceId(CodoonHealthDevice parsedAd){
        int index = -1;
        byte[] infos = parsedAd.manufacturer;
        if(infos.length < 14) return;

        parsedAd.id = GpsBandParseUtil.getDeviceId(infos);
    }

    private boolean isRomDevice(String deviceName) {

        if(null == deviceName) return  false;

        boolean is =
                deviceName.equals("codoon")
                        || deviceName.startsWith("cod_");
        return is;
    }

    public void timeOutAction() {
        boolean isinterrupt = false;
        if (null != mOnSeartchCallback) {
            isinterrupt = mOnSeartchCallback.onSeartchTimeOut();
        }

        if (!isinterrupt) {

            stopSearch();
        }
    }

    public int getTime_out() {
        return time_out;
    }

    public void setTime_out(int time_out) {
        this.time_out = time_out;
    }


    public void setOnSeartchCallback(OnDeviceSeartchCallback onSeartchCallback) {
        this.mOnSeartchCallback = onSeartchCallback;
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

                Bundle bd = intent.getExtras();
                if (bd == null) {
                }
                Set<String> keys = bd.keySet();
                for (String s : keys) {
                    try {
                        CLog.i("auth", "key " + s + " value:" + bd.getString(s));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (null != mOnSeartchCallback) {
                    CodoonHealthDevice mDevice = new CodoonHealthDevice();

                    mOnSeartchCallback.onSeartch(mDevice, null);
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {

                timeOutAction();
            }
        }
    };



}
