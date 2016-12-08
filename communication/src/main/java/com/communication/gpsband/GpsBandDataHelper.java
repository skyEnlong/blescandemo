package com.communication.gpsband;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.communication.bean.GpsBleFileBean;
import com.communication.bean.GpsDetailInfo;
import com.communication.bean.GpsSummaryInfo;
import com.communication.data.AccessoryDataParseUtil;
import com.communication.data.AccessoryValues;
import com.communication.data.CLog;
import com.communication.util.CodoonEncrypt;
import com.communication.util.FileDES;
import com.communication.util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by workEnlong on 2015/12/4.
 */
public class GpsBandDataHelper {

    private final String TAG = "gps_ble";
    private Context mContext;
    private GpsBandCallBack callBack;
    private List<GpsBleFileBean> needParseFiles;
    private List<GpsBleFileBean> hasParseFiles;
    private String filePath;
    private List<GpsSummaryInfo> summaryTmp;
    private List<GpsDetailInfo> detailTmp;
    private Handler mHandler;
    private FileDES secretUtil;

    private final int PARSE_NEXT = 0xababab;


    public GpsBandDataHelper(Context mContext, GpsBandCallBack callBack) {
        this.callBack = callBack;
        this.mContext = mContext;
        needParseFiles =Collections.synchronizedList( new ArrayList<GpsBleFileBean>());
        hasParseFiles = new ArrayList<GpsBleFileBean>();


        summaryTmp = new ArrayList<GpsSummaryInfo>();
        detailTmp = new ArrayList<GpsDetailInfo>();

        filePath = FileUtil.getDataFilePath(mContext);
        try {

            secretUtil = new FileDES(SecretKeyUtil.getRealKey(mContext));
        }catch (Exception e){
            e.printStackTrace();
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PARSE_NEXT:
                        parseData();
                        break;
                }

            }
        };

        //get last sync saved
        File file = new File(filePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files && files.length > 0) {
                for (File f : files) {
                    String file_name = f.getName();
                    String[] items = file_name.split("_");
                    if (null != items && items.length == 3) {
                        GpsBleFileBean bean = new GpsBleFileBean();
                        bean.address = items[2];
                        bean.name = items[1];
                        bean.flag = items[0];
                        needParseFiles.add(bean);
                    }
                }
            }
        }
    }

    public void saveData(GpsBleFileBean bean, byte[] data) {

        needParseFiles.add(bean);
        String file_name = filePath + File.separator +
                bean.flag + "_" + bean.name + "_" + bean.address;

        try {

            CLog.i(TAG, "save file :" + file_name);

            FileUtil.saveAsFile(file_name, data);
         } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] desDecrypt(GpsBleFileBean bean, byte[] data){
        if(CLog.isDebug){  // debug mode , save the source ecrypt files

            String file_name_encrypt = filePath + File.separator +
                    "encrypt" + "_" +bean.flag + "_" + bean.name + "_" + bean.address;
            FileUtil.saveAsFile(file_name_encrypt, data);
        }

        byte[] real = null;
        try {

            real = desDecrypt(data);

        }catch (Exception e){
            CLog.e(TAG, "descrypt err:" + e.getMessage());
        }

        return removeEndZeros(bean, real);
    }
    public byte[] desDecrypt(byte[] data) throws GeneralSecurityException {

        return secretUtil.desDecrypt(data, SecretKeyUtil.getRealKey(mContext));
    }

    public List<GpsBleFileBean> getNeedParseFiles() {
        return needParseFiles;
    }

    public void dealData() {
        mHandler.sendEmptyMessage(PARSE_NEXT);

    }

    private void parseData() {
        CLog.i(TAG, "start new thread to parse data");
        new Thread(parseRunnable).start();
    }

    private GpsDetailInfo parseDetailData(byte[] data) {
        return GpsBandParseUtil.parseDetailData(data);
    }

    private GpsSummaryInfo parseSummaryData(byte[] data) {
        return GpsBandParseUtil.parseSummaryInfo(data);
    }

    private void parseStepData(byte[] bytes) {
        if(null == bytes) return;
//        deCryptData(bytes);

        ByteArrayOutputStream mBaos = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {

            mBaos.write(encryptMyxor((bytes[i] & 0xff), mBaos.size() % 6));
        }

        AccessoryDataParseUtil decode = AccessoryDataParseUtil.getInstance(mContext);
        HashMap<String, AccessoryValues> data = decode.analysisDatas(bytes);
        callBack.onSyncDataOver(data, mBaos);

    }




    public String encryptContent(File file){
        InputStream out = null;

        String outFile = file.getAbsolutePath() + "_encry";
        try {

            secretUtil.encryptFile(file.getAbsolutePath(), outFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return outFile;
    }

    private byte encryptMyxor(int original, int n) {
        return CodoonEncrypt.encryptMyxor(original, n);
    }

    private Runnable parseRunnable = new Runnable() {
        @Override
        public void run() {

            if (needParseFiles.size() > 0) {
                GpsBleFileBean bean = needParseFiles.get(0);
                needParseFiles.remove(bean);

                CLog.i(TAG, "begin parse:" + bean.flag + "_" + bean.name);

                String file_name = filePath + File.separator +
                        bean.flag + "_" + bean.name + "_" + bean.address;
                byte[] data = null;
                try {
                    data = FileUtil.readFile(file_name);

                } catch (IOException e) {
                    e.printStackTrace();
                    CLog.e(TAG, e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (bean.flag.equalsIgnoreCase(GpsBandConst.FLAG_N)) {
                    parseStepData(data);
                    deleteFile(bean);

                } else if (bean.flag.equalsIgnoreCase(GpsBandConst.FLAG_S)) {
                    CLog.i(TAG, "parse summary :" + bean.name);
                    GpsSummaryInfo inf = parseSummaryData(data);
                    if (null != inf) {
                        inf.data_id = bean.name + bean.address;

                        boolean hasFoundDetail = false;
                        //to find detail ,
                        for (GpsDetailInfo detail : detailTmp) {
                            if (detail.data_id.equals(inf.data_id)) {

                                CLog.i(TAG, "found detail :" + bean.name);

                                hasFoundDetail = true;

                                inf.detailInfo = detail;

                                deleteFile(bean);

                                callBack.onGetGpsInfo(inf);
                                break;
                            }
                        }

                        // not find, put in tmp
                        if (!hasFoundDetail) {
                            summaryTmp.add(inf);
                            hasParseFiles.add(bean);
                            CLog.i(TAG, "not found detail :" + bean.name);

                        }

                    }else{// delete not right detail
                        hasParseFiles.add(bean);
//                        deleteFile(bean);
                    }


                } else if (bean.flag.equalsIgnoreCase(GpsBandConst.FLAG_G)) {
                    GpsDetailInfo info = parseDetailData(data);
                    if (null != info) {
                        info.data_id = bean.name + bean.address;
                        CLog.i(TAG, "parse detail :" + bean.name);


                        boolean hasFoundSummary = false;
                        for (GpsSummaryInfo summaryInfo : summaryTmp) {
                            if (summaryInfo.data_id.equals(info.data_id)) {
                                CLog.i(TAG, "found summary :" + bean.name);

                                //delete file on disk
//                                    FileUtil.deleteFile(file_name);
                                hasFoundSummary = true;

                                summaryInfo.detailInfo = info;

                                deleteFile(bean);

                                callBack.onGetGpsInfo(summaryInfo);

                                break;
                            }
                        }

                        if (!hasFoundSummary) {
                            CLog.i(TAG, "not found summary :" + bean.name);

                            hasParseFiles.add(bean);
                            detailTmp.add(info);
                        }
                    }else{
                        hasParseFiles.add(bean);

                    }
                }


                if (needParseFiles.size() > 0) {

                    mHandler.sendEmptyMessage(PARSE_NEXT);
                } else {
                    if (hasParseFiles.size() > 0) {

                        String str = "";
                        for (GpsBleFileBean bean1 : hasParseFiles) {
                            str += bean1.flag + bean1.name;
//                            deleteFile(bean1);
                        }
                        CLog.i(TAG, "files not find pair.." + str);
                    }
                }
            }
        }
    };

    private void deleteFile(GpsBleFileBean bean) {
        hasParseFiles.remove(bean);

        GpsBleFileBean needDelete = null;

        if(bean.flag.equals(GpsBandConst.FLAG_G) ||
                bean.flag.equals(GpsBandConst.FLAG_S) ){
            for(GpsBleFileBean b : hasParseFiles){
                if(b.name.equals(bean.name) && b.address.equals(bean.address)){
                    needDelete= b;
                    break;
                }
            }

        }

        String file_name = filePath + File.separator +
                bean.flag + "_" + bean.name + "_" + bean.address;


        if(CLog.isDebug){
            FileUtil.renameFile(filePath, file_name);
        }

        FileUtil.deleteFile(file_name);

        if(null != needDelete){
            hasParseFiles.remove(needDelete);
            String file_name_de = filePath + File.separator +
                    needDelete.flag + "_" + needDelete.name + "_" + needDelete.address;

            if(CLog.isDebug){
                FileUtil.renameFile(filePath, file_name_de);
            }


            FileUtil.deleteFile(file_name_de);

        }

    }

    /**
     *
     * @param data
     * @param lastCrc init 0
     * @return
     */
    public int getCrc(byte[] data, int lastCrc){
        int CRC_DEF_POLY = 0x8005;
        int crc = lastCrc;
        for(int j = 0; j < data.length; j++){
            int uc = data[j] & 0x0ff ;
            for(int i = 0; i < 8; i++){

                crc = (((( uc ^ ( crc >>8)) & 0x80) & 0xff) > 0) ?
                        (( crc << 1)^CRC_DEF_POLY)
                        : (crc << 1);
                uc <<=1;
            }
        }
        return crc & 0xffff;
    }

    /**
     * #define CRC_DEF_POLY 0x8005
     2
     3 * @brief 计算crc
     4 * @param src 数据源
     5 * @param length 数据源长度, 单位字节
     6 * @param crc 初始值，设为0
     7 * @return 数据源的计算结果
     8
     9 unsigned short Crc16Compute(unsigned char *src, unsigned int length , unsigned short
     crc )
     10 {
     11 unsigned char ； uc
     12 for ( unsigned int j = 0; j < length ; j ++)
     13 {
     14 uc = *( src +j ) ;
     15 for ( unsigned int i = 0; i < 8; i ++)
     16 {
     17 crc = (( uc^(unsigned char)( crc >>8))&0x80) ? (( crc << 1)
     ^CRC_DEF_POLY) : (crc << 1) ;
     18 uc <<=1;
     19 }
     20 }
     21 return crc ;
     22 }

     *
     */

    /**
     * as the file has encrypt add 0 to fill 8 * n, we must remove the last zeors
     * @param data
     * @return
     */
    private byte[] removeEndZeros(GpsBleFileBean beans, byte[] data){
        if(null == data) return null;
        byte[] real = null;
        int real_len = beans.size;

        if(real_len == 0){
            if(beans.flag.equalsIgnoreCase(GpsBandConst.FLAG_N)){
                real_len  = data.length / 6 * 6;   // N align with 6 bytes
            }else if(beans.flag.equalsIgnoreCase(GpsBandConst.FLAG_G)){  //G align with 11 bytes
                real_len  = data.length / GpsBandParseUtil.FREAME_LENGTH *  GpsBandParseUtil.FREAME_LENGTH;
            }else {
                real_len = data.length;
            }
        }


        if(real_len != data.length){
            real = new byte[real_len];
            System.arraycopy(data, 0, real, 0, real_len);
        }else {
            real = data;
        }

        return real;
    }
}
