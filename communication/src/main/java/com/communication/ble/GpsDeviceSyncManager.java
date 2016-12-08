package com.communication.ble;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.communication.bean.GpsBleFileBean;
import com.communication.data.CLog;
import com.communication.gpsband.GpsBandCallBack;
import com.communication.gpsband.GpsBandCommandHelper;
import com.communication.gpsband.GpsBandConst;
import com.communication.gpsband.GpsBandDataHelper;
import com.communication.gpsband.GpsBandParseUtil;
import com.communication.gpsband.SecretKeyUtil;
import com.communication.util.CommonUtils;
import com.communication.util.FileUtil;
import com.communication.util.MobileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by workEnlong on 2015/12/3.
 */
public class GpsDeviceSyncManager extends BaseDeviceSyncManager {


    private int FREAME_SIZE_DEFAULT = 15;  // each frame size
    private int FREAME_NUM_DEFAULT = 16;   // frame's num send each

    private GpsBandCommandHelper mCommandHelper;

    private int totalFileCount;
    private int totalFrame;
    private int fileFrame;
    private int frameIndex;
    private List<GpsBleFileBean> files;
    private ByteArrayOutputStream receiveStream;
    private GpsBandDataHelper mGpsBandDataHelper;

    private long totalEphemerisSize;
    private String mEphemerisFilePath;
    private String mEphemerisFileName;
    private byte[] mEphemerisContent;
    private int curEphemerisFrameIndex;
    private int crc = 0;

    private boolean isWriteEphemeris;
    private GpsBandCallBack mCallBack;
    private GpsBleFileBean curSelectFile;

    private boolean isNeedCrypt = true;

    public GpsDeviceSyncManager(Context mContext, final GpsBandCallBack mCallBack) {
        super(mContext, mCallBack);
        this.mCallBack = mCallBack;
        files = new ArrayList<GpsBleFileBean>();
        mGpsBandDataHelper = new GpsBandDataHelper(mContext, mCallBack);
        receiveStream = new ByteArrayOutputStream();
        mCommandHelper = new GpsBandCommandHelper();
        isNeedCrypt = MobileUtil.getBooleanMetaValue(mContext, "GPS_BAND_ENCRYPT");
        mTimeoutCheck.setTimeout(15000);

    }

    protected BaseBleManager initBleManager() {
        bleManager = new GpsBandBleManager(mContext);

        bleManager.setConnectCallBack(this);
        bleManager.setWriteCallback(this);

        return bleManager;
    }

    @Override
    public boolean handMessage(Message msg) {
        return false;
    }


    public void startSyncData() {
        //communication with key
        isStart = true;
        if (isNeedCrypt) {

            writeDataToDevice(mCommandHelper.getCommand(GpsBandConst.CODE_SECRET_KEY,
                    SecretKeyUtil.getDynamicKey(mContext)));
        } else {

            writeDataToDevice(mCommandHelper.getCommand(GpsBandConst.CODE_FILE_COUNT));
        }

    }


    /**
     * default path is sdcard/codoonsports/accessory/Ephemeris
     *
     * @param file_name
     */
    public void startSyncEphemeris(String file_name) {
        isStart = true;
        mEphemerisFileName = file_name;
        crc = 0;
        queryEphemerisFrame(0);
    }


    @Override
    public void onWriteSuccess() {
        CLog.i(TAG, "onWriteSuccess ");
        if (!isWriteEphemeris) {
            CLog.i(TAG, "not write ep..");
            return;
        }

        curEphemerisFrameIndex++;
        CLog.i(TAG, "onWriteSuccess next");
        writeEphemerisByFrame(curEphemerisFrameIndex, true);

    }

    private void queryEphemerisFrame(int cutFrameIndex) {
        CLog.i(TAG, "queryEphemerisFrame:" + cutFrameIndex);
        isWriteEphemeris = false;
        writeDataToDevice(mCommandHelper.getWriteFileQueryCommand(
                mEphemerisFileName.substring(0, 1),
                mEphemerisFileName.substring(1),
                cutFrameIndex));

    }


    @Override
    protected void dealResponse(byte[] data) {

        if (null == data || data.length < 2) return;
        if ((data[0] & 0xff) == (0xAA)) {
            dealResCommand(data);
        } else if ((data[0] & 0xf0) == (0xB0)) {

            dealDataContent(data);
        }

    }


    /**
     * deal other command response
     *
     * @param data
     */
    private void dealResCommand(byte[] data) {
        switch (data[1] & 0xff) {
            case GpsBandConst.RES_BIND:
                int result = data[3];
                CLog.i(TAG, "bind result:" + result);
                if (result != 0) {

                    mCallBack.onBindSucess();
                } else {
                    mCallBack.onTimeOut();
                }
                break;

            case GpsBandConst.RES_READ_VERSION:
                String hVersion = (data[4] & 0xff) + "." + (data[5] & 0xff);
                mCallBack.onGetVersion(hVersion);
                break;

            case GpsBandConst.RES_READ_ID:
                int len = data[2] & 0xff;
                byte[] idByte = Arrays.copyOfRange(data, 3, len + 3);
                mCallBack.onGetDeviceID(GpsBandParseUtil.getDeviceId(idByte));
                break;

            case GpsBandConst.RES_SECRET_KEY:
                CLog.i(TAG, "get key response,  to get  file count");
                writeDataToDevice(mCommandHelper.getCommand(GpsBandConst.CODE_FILE_COUNT));

                break;
            case GpsBandConst.RES_FILE_COUNT:
                //内容 0xAA 0x8C 0x06 NUM0 NUM1 FRAME0 FRAME1 FRAME2 FRAME3 校验和
                totalFileCount = ((data[3] & 0xff) << 8) + (data[4] & 0xff);
                totalFrame = ((data[5] & 0xff) << 24) + ((data[6] & 0xff) << 16) +
                        ((data[7] & 0xff) << 8) + (data[8] & 0xff);

                CLog.i(TAG, "get totalFileCount:" + totalFileCount +
                        " totalFrame:" + totalFrame);
                files.clear();

                if (totalFileCount == 0) {
                    stop();
                    mCallBack.onClearDataSuccessed();
                    return;
                }

                writeDataToDevice(mCommandHelper.getCommand(
                        GpsBandConst.CODE_FILE_INFO));
                break;

            case GpsBandConst.RES_FILE_INFO:

                int length = (data[2] & 0xff);

                if (length >= 5) {

                    int curIndex = 3;
                    for (int i = 0; i < length; i += 5) {

                        String flag = new String(data, curIndex, 1);

                        curIndex += 1;
                        GpsBleFileBean bean = new GpsBleFileBean();
                        bean.name = String.valueOf(((data[curIndex] & 0xff) << 24) +
                                ((data[curIndex + 1] & 0xff) << 16) +
                                ((data[curIndex + 2] & 0xff) << 8) +
                                (data[curIndex + 3] & 0xff));

                        bean.flag = flag;

                        curIndex += 4;
                        CLog.i(TAG, "get file name :" + flag + bean.name);
                        if (!files.contains(bean)) {

                            files.add(bean);
                        }

                    }
                } else {
                    stop();
                    mCallBack.onClearDataSuccessed();
                    return;
                }

                if (totalFileCount > files.size()) {
                    //file message not collect over
                    CLog.i(TAG, "totalFileCount :" + totalFileCount + " getfilecount:" + files.size());

                    writeDataToDevice(mCommandHelper.getCommand(
                            GpsBandConst.CODE_FILE_INFO));
                } else if (files.size() > 0) {
                    //get all file info, begin load files
                    CLog.i(TAG, "all file name get, begin to  select file");
                    loadFile();
                }


                break;

            case GpsBandConst.RES_FILE_SELECT:
                // get file frame count
                int index = 3;
                fileFrame = ((data[index] & 0xff) << 16) +
                        ((data[index + 1] & 0xff) << 8) + (data[index + 2] & 0xff);
                frameIndex = 0;
                if (fileFrame < FREAME_NUM_DEFAULT) {
                    frameIndex = fileFrame - 1;
                } else {
                    frameIndex = FREAME_NUM_DEFAULT - 1;
                }
                receiveStream.reset();

                int data_len = data[2];
                index += 3;
                if (data_len > 3) {
                    curSelectFile.size = ((data[index] & 0xff) << 24) +
                            ((data[index + 1] & 0xff) << 16) +
                            ((data[index + 2] & 0xff) << 8) +
                            (data[index + 3] & 0xff);
                }

                CLog.i(TAG, "file_frame is:" + fileFrame +                                "target frame index:" + frameIndex +
                                "  file size:" + curSelectFile.size
                );


                if (frameIndex == 0) {
                    mCallBack.onClearDataSuccessed();
                    stop();
                } else {

                    upLoadContent(0);
                }
                break;

            case GpsBandConst.RES_CLEAR_DATA:
                loadFile();
                break;

            case GpsBandConst.RES_PROGRESS_QUERY:
                int i = 3;
                String name_flag = new String(data, i, 1);
                i += 1;
                byte[] name_byte = Arrays.copyOfRange(data, i, i + 4);
                String name = CommonUtils.convertByteToHexString(name_byte);
                i += 4;
                int progress = ((data[i] & 0xff) << 16) +
                        ((data[i + 1] & 0xff) << 8) +
                        (data[i + 2] & 0xff);
                mCallBack.onEphemerisProgressQurey(progress, name);
                CLog.i(TAG, "query response name" + name_flag + name + " frame:" + progress);

                curEphemerisFrameIndex = progress;
                writeEphemerisByFrame(progress, false);
                break;
            case GpsBandConst.RES_PROGRESS_RESET:
                curEphemerisFrameIndex = 0;
                writeEphemerisByFrame(0, false);
                break;

            case GpsBandConst.RES_FILE_CHECK:
                int cur_index = 3;
                String r_name_flag = new String(data, cur_index, 1);
                cur_index += 1;
                byte[] r_name_byte = Arrays.copyOfRange(data, cur_index, cur_index + 4);
                String r_name = null;
                if (r_name_flag.equalsIgnoreCase(GpsBandConst.FLAG_E)) {
                    r_name = CommonUtils.convertByteToHexString(r_name_byte);
                } else {
                    int curIndex = 0;
                    r_name = String.valueOf(((r_name_byte[curIndex] & 0xff) << 24) +
                            ((r_name_byte[curIndex + 1] & 0xff) << 16) +
                            ((r_name_byte[curIndex + 2] & 0xff) << 8) +
                            (r_name_byte[curIndex + 3] & 0xff));


                }

                cur_index += 4;


                int resCrc = data[cur_index] & 0xff;

                dealWithCrcReult(r_name_flag, r_name, resCrc);

                break;
        }
    }

    private void dealWithCrcReult(String r_name_flag, String r_name, int resCrc) {
        CLog.i(TAG, "file" + r_name_flag + r_name + " crc check status:" + resCrc);

        if (r_name_flag.equalsIgnoreCase(GpsBandConst.FLAG_E)) {

            //1 success
            mCallBack.onEphemerisUpdateSuccess();
            mCallBack.onCrcCheckResult(resCrc);
            stop();
        } else if (null != curSelectFile) {

            GpsBleFileBean bean = curSelectFile;
            if (curSelectFile.flag.equals(r_name_flag) &&
                    curSelectFile.name.equals(r_name)) {
                files.remove(bean);

                if (resCrc == 1) {
                    CLog.e(TAG, bean.flag + bean.name + " check crc failed");

                    if (CLog.isDebug)
                        CLog.r(bean.flag + bean.name, receiveStream.toByteArray());

                    receiveStream.reset();
                    loadFile();
                } else {

                    saveFiles(bean, receiveStream.toByteArray());
                    deleteBandFiles(bean);
                    receiveStream.reset();
                }
            } else {
                CLog.e(TAG, "response name not equal current file name");
            }


        } else {
            CLog.e(TAG, " not select file");
        }
    }

    private void writeEphemerisByFrame(int frame, boolean isNeedQuery) {
        isWriteEphemeris = true;
        CLog.i(TAG, "need write frame : " + frame);

        if (TextUtils.isEmpty(mEphemerisFilePath)) {
            mEphemerisFilePath = FileUtil.getEphemerisPath(mContext);
        }


        if (null == mEphemerisContent) {
            try {
                File file = new File(mEphemerisFilePath + File.separator + mEphemerisFileName);
                if (!file.exists()) {
                    CLog.e(TAG, "not find file:" + file.getAbsolutePath());
                    mCallBack.onCrcCheckResult(0);
                    stop();
                    return;
                }

                totalEphemerisSize = file.length();
                mEphemerisContent = FileUtil.readFile(file.getAbsolutePath());

                CLog.i(TAG, " total file size " + totalEphemerisSize);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (null == mEphemerisContent) {
            CLog.e(TAG, "mEphemerisContent is null");
            mCallBack.onCrcCheckResult(0);
            stop();
            return;
        }

        int curSize = frame * FREAME_SIZE_DEFAULT;
        if (isNeedQuery && frame != 0 && frame % (FREAME_NUM_DEFAULT) == 0
                && curSize < totalEphemerisSize) {
            CLog.i(TAG, "need query frame:" + frame);
            isWriteEphemeris = false;
            queryEphemerisFrame(frame);
            return;
        }


        byte[] data = null;
        int data_len = 0;
        int curIndex = (frame + 1) * FREAME_SIZE_DEFAULT;
        if (curIndex <= totalEphemerisSize) {
            CLog.i(TAG, "cur frame full 15");
            data_len = FREAME_SIZE_DEFAULT;

        } else if (curIndex - totalEphemerisSize < FREAME_SIZE_DEFAULT &&
                totalEphemerisSize - curSize > 0) {
            CLog.i(TAG, "cur frame not full 15,just have " + (totalEphemerisSize - curSize));
            data_len = (int) (totalEphemerisSize - curSize);

        } else {
            CLog.i(TAG, "has write over, send file crc " + Integer.toHexString(crc));

            isWriteEphemeris = false;
            if (isNeedQuery) {

                checkCrcByfileName(mEphemerisFileName, crc);
            } else {
                mCallBack.onEphemerisUpdateSuccess();
            }
            return;
        }

        try {
            data = Arrays.copyOfRange(mEphemerisContent, curSize, curSize + data_len);
        } catch (Exception e) {
            stop();
            mCallBack.onCrcCheckResult(0);  //notice ui update
            e.printStackTrace();
            CLog.e(TAG, e.getMessage());
        }
        CLog.i(TAG, " write current size begin: " + frame * FREAME_SIZE_DEFAULT + " curFrame:" + frame);
        crc = mGpsBandDataHelper.getCrc(data, crc);

        CLog.i(TAG, " crc is " + Integer.toHexString(crc));

        writeDataToDevice(mCommandHelper.getWriteFileContent(frame, data));
    }

    private void checkCrcByfileName(String mFileName, int crc) {

        CLog.i(TAG, "to check file:" + mFileName + " crc :" + crc);
        byte[] nameByte = null;
        if (mFileName.startsWith("E")) {
            nameByte = CommonUtils.convertHexStringToByte(
                    mFileName.substring(1));

        } else {
            nameByte = new byte[4];
            int name = Integer.parseInt(mFileName.substring(1));
            nameByte[0] = (byte) ((name >> 24) & 0xff);
            nameByte[1] = (byte) ((name >> 16) & 0xff);
            nameByte[2] = (byte) ((name >> 8) & 0xff);
            nameByte[3] = (byte) ((name) & 0xff);

        }

        writeDataToDevice(mCommandHelper.getCommand(
                GpsBandConst.CODE_FILE_CHECK,
                new byte[]{mFileName.getBytes()[0],
                        nameByte[0],
                        nameByte[1],
                        nameByte[2],
                        nameByte[3],
                        (byte) ((crc >> 8) & 0xff),
                        (byte) ((crc) & 0xff)}
        ));
    }

    /**
     * get file frame data
     *
     * @param frame_start
     */
    private void upLoadContent(int frame_start) {

        byte[] bytes = new byte[]{
                (byte) ((frame_start >> 16) & 0xff),
                (byte) ((frame_start >> 8) & 0xff),
                (byte) (frame_start & 0xff)};
        writeDataToDevice(mCommandHelper.getCommand(GpsBandConst.CODE_FILE_UPLOAD,
                bytes));
    }

    /**
     * deal with file data
     *
     * @param data
     */
    private void dealDataContent(byte[] data) {
        if (!checkValid(data)) return;
        mTimeoutCheck.startCheckTimeout();
        int length = (data[0] & 0x0f);
        int loadFrame = ((data[1] & 0xff) << 16) +
                ((data[2] & 0xff) << 8) + (data[3] & 0xff);

        receiveStream.write(data, 4, length);

        CLog.i(TAG, "get frame:" + loadFrame + " target index:" + frameIndex);
        if (loadFrame == frameIndex) {

            // to get next 15 frame
            if (frameIndex < fileFrame - 1) {
                if (frameIndex + FREAME_NUM_DEFAULT < fileFrame) {
                    frameIndex += FREAME_NUM_DEFAULT;
                } else {
                    frameIndex = fileFrame - 1;
                }
                CLog.i(TAG, " frame_index:" + frameIndex + " file_frame:" + fileFrame);
                upLoadContent(loadFrame + 1);
            } else {

                // delete and to get NextFile  not check the file
//                GpsBleFileBean bean = files.get(files.size() - 1);
//                saveFiles(bean, receiveStream.toByteArray());
//                receiveStream.reset();
//                curDecrptIndex = 0;
//                files.remove(bean);
//                deleteBandFiles(bean);

                mTimeoutCheck.stopCheckTimeout();
                curSelectFile.address = device.getAddress();
                byte[] decrypt_data = null;

                if (isNeedCrypt) {
                    CLog.i(TAG, "begin to decrypt");
                    decrypt_data = mGpsBandDataHelper.desDecrypt(curSelectFile, receiveStream.toByteArray());
                    receiveStream.reset();
                    receiveStream.write(decrypt_data, 0, decrypt_data.length);
                } else {
                    decrypt_data = receiveStream.toByteArray();
                }

                int crc = mGpsBandDataHelper.getCrc(decrypt_data, 0);
                decrypt_data = null;
                checkCrcByfileName(curSelectFile.flag + curSelectFile.name, crc);

            }

        } else if (loadFrame > frameIndex) {
            mTimeoutCheck.stopCheckTimeout();
            CLog.e(TAG, "frame receive num has out of protocol's num:" + loadFrame + "-->" + frameIndex);
        }
    }


    private void saveFiles(final GpsBleFileBean bean, final byte[] bytes) {

        bean.address = device.getAddress();
        mGpsBandDataHelper.saveData(bean, bytes);

    }


    /**
     * load each file frame info
     */
    private void loadFile() {
        mGpsBandDataHelper.dealData();

        if (files.size() == 0) {
            //all files upload, means sync over
            CLog.i(TAG, "all file has load");

            stop();

            if (null != mCallBack) mCallBack.onClearDataSuccessed();

            //deal data
        } else {
            // select file
            curSelectFile = files.get(files.size() - 1);
            int file_name = Integer.parseInt(curSelectFile.name);
            byte[] bytes = new byte[]{(byte) (curSelectFile.flag.getBytes()[0] & 0xff),
                    (byte) ((file_name >> 24) & 0xff),
                    (byte) ((file_name >> 16) & 0xff),
                    (byte) ((file_name >> 8) & 0xff),
                    (byte) ((file_name & 0xff))
            };
            CLog.i(TAG, "load file:" + curSelectFile.flag + curSelectFile.name);
            writeDataToDevice(mCommandHelper.getCommand(GpsBandConst.CODE_FILE_SELECT,
                    bytes));
        }

    }

    private void deleteBandFiles(GpsBleFileBean bean) {
        CLog.i(TAG, "delete file " + bean.flag + bean.name);

        int file_name = Integer.parseInt(bean.name);

        byte[] bytes = new byte[]{(byte) (bean.flag.getBytes()[0] & 0xff),
                (byte) ((file_name >> 24) & 0xff),
                (byte) ((file_name >> 16) & 0xff),
                (byte) ((file_name >> 8) & 0xff),
                (byte) ((file_name & 0xff))
        };
        writeDataToDevice(mCommandHelper.getCommand(GpsBandConst.CODE_CLEAR_DATA,
                bytes));
    }


    public void stop() {
        super.stop();
        curSelectFile = null;
        isWriteEphemeris = false;

        crc = 0;
        curEphemerisFrameIndex = 0;
        mEphemerisFilePath = null;
        mEphemerisFileName = null;
        if (null != mEphemerisContent) {
            mEphemerisContent = null;

        }
        frameIndex = 0;
        totalFileCount = 0;
        receiveStream.reset();
        files.clear();

        totalFrame = 0;
        fileFrame = 0;

    }


}
