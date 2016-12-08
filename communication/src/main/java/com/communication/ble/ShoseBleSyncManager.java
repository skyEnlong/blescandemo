package com.communication.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.communication.bean.ShoseDataDetail;
import com.communication.common.BaseCommandHelper;
import com.communication.data.CLog;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.data.IShoesSyncCallBack;
import com.communication.data.TransferStatus;
import com.communication.shoes.ShoesParseHelper;
import com.communication.shoes.ShoseCommand;
import com.communication.shoes.ShoseUpGradeMangaer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by workEnlong on 2016/1/21.
 */
public class ShoseBleSyncManager extends CodoonBleSyncManager {

    private IShoesSyncCallBack shoseCallBack;
    private int shoseDataIndex;
    private int shoseDataFrame;
    private int alginType;
    private BaseCommandHelper commandHelper;
    private ShoseUpGradeMangaer mShoseUpGradeMangaer;


    public ShoseBleSyncManager(Context context, IShoesSyncCallBack callback) {
        super(context, callback);
        this.shoseCallBack = callback;
        commandHelper = new BaseCommandHelper();
    }

    @Override
    public void analysis(ArrayList<Integer> datas) {

        if(null != datas && datas.size() > 3){

            int res_code = datas.get(1);
            CLog.i(TAG, "res code:" + Integer.toHexString(res_code)
                            + " len:" + datas.get(2)
                            + " data_size:" + datas.size()

            );


            ArrayList<Integer> content = new ArrayList<>(datas.subList(3, 3 + datas.get(2)));

            CLog.i(TAG, "content size:" + content.size());
            switch (res_code){
                case ShoseCommand.RES_CONNECT:
                    stopTimeCheckOut();
                    shoseCallBack.onBindSucess();
                    break;
                case ShoseCommand.RES_SHOES_SUMMARY:
                    CLog.i(TAG, "get res summary");

                    try {

                        shoseCallBack.onGetShoseSummary(
                                ShoesParseHelper.parseSummary(content));

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    writeDataToDevice(commandHelper.getCommand(
                            ShoseCommand.CODE_SHOES_FRAME_DETAIL));


                    break;

                case ShoseCommand.RES_SHOES_DETAIL_FRAME:
                    alginType = content.get(0);
                    shoseDataFrame = (content.get(1) << 8) + content.get(2);
                    shoseDataIndex = 0;
                    CLog.i(TAG, "get shose frame:" + shoseDataFrame);
                    if(shoseDataFrame > 0){
                        if(null == mRecordDatas){
                            mRecordDatas = new ArrayList<ArrayList<Integer>>();
                        }else{
                            mRecordDatas.clear();
                        }

                        getShoseDataByFrame(shoseDataIndex);
                    }else {
                        stop();
                        shoseCallBack.onGetShoseClear();
                    }

                    break;

                case ShoseCommand.RES_SHOES_DETAIL:
                    CLog.i(TAG, "get shose frame data :" + shoseDataIndex);
                    shoseDataIndex++;

                    mRecordDatas.add(content);
                    if(shoseDataIndex < shoseDataFrame){
                        getShoseDataByFrame(shoseDataIndex);
                    }else{

                        parseDetails(mRecordDatas);
                        writeDataToDevice(commandHelper.getCommand(ShoseCommand.CODE_SHOES_CLEAR));

                    }
                    break;

                case ShoseCommand.RES_SHOES_CLEAR:

                    shoseCallBack.onGetShoseClear();
//                    getDeviceTotalInfo();
                    break;

                case ShoseCommand.RES_SHOES_COST:
                    int dis = ((content.get(0) & 0xff) << 24) +
                            ((content.get(1) & 0xff) << 16) +
                            ((content.get(2) & 0xff) << 8) +
                            (content.get(3) & 0xff );

                    shoseCallBack.onGetShoseTotal(dis);
                    stop();
                    break;

                case TransferStatus.RECEIVE_GETVERSION_ID:
                    mTimeoutCheck.stopCheckTimeout();
                    String hVersion = content.get(1) + "." + content.get(2);
                    String hard_version = "";
                    if(content.size() == 5){
                        hVersion = hVersion + "." + content.get(3) + "." + content.get(4);
                    }

//                    String ware_string = (TextUtils.isEmpty(hard_version)) ? hVersion :
//                            hVersion + "_" + hard_version;
                    mISyncDataCallback.onGetVersion(hVersion);
                    break;

                default:
                    super.analysis(datas);

                    break;
            }

        }

        return ;
    }

    public void getDeviceTotalInfo(){

        writeDataToDevice(commandHelper.getCommand(ShoseCommand.CODE_SHOES_COST));
    }



    private void parseDetails(ArrayList<ArrayList<Integer>> details) {

        List<ShoseDataDetail> shoseDataDetails = null;

        try {

            shoseDataDetails = ShoesParseHelper.parseShoseDetail(details);

        }catch (Exception e){
            CLog.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        if(null != shoseCallBack){

            try {

                shoseCallBack.onGetShoseDetail(shoseDataDetails);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }


    private void getShoseDataByFrame(int shoseDataIndex) {
        byte[] bytes = new byte[]{(byte)((shoseDataIndex >> 8) & 0xff),
                (byte)(shoseDataIndex & 0xff)
        };
        writeDataToDevice(commandHelper.getCommand(
                ShoseCommand.CODE_SHOES_DETAIL,
                bytes));

    }

    @Override
    public void getValue(){
        writeDataToDevice(commandHelper.getCommand(
                ShoseCommand.CODE_SHOES_SUMMARY));
    }

    @Override
    public void stopUpgrade() {
        if(null != mShoseUpGradeMangaer) mShoseUpGradeMangaer.stop();
        super.stopUpgrade();

    }

    @Override
    public void startUpgrade(BluetoothDevice device, String bootfile, DeviceUpgradeCallback upgradeCallback) {
        isStartBoot = true;
        CLog.i(TAG, "startUpgrade file:" + bootfile);
        mHandler.removeMessages(SEND_DATA);
        lastData = null;
        mTimeoutCheck.stopCheckTimeout();
        if(null != mShoseUpGradeMangaer){
            mShoseUpGradeMangaer.stop();
            mShoseUpGradeMangaer = null;
        }
        mShoseUpGradeMangaer = new ShoseUpGradeMangaer(mContext,
                upgradeCallback, bleManager, mISyncDataCallback);

        mShoseUpGradeMangaer.setUpgradeFilePath(bootfile);
        if(bleManager.isConnect){

            mShoseUpGradeMangaer.startUpgrade();
        }else {
            mShoseUpGradeMangaer.startDevice(device);
        }
    }


}
