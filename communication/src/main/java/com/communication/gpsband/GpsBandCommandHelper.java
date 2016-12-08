package com.communication.gpsband;

import com.communication.common.BaseCommandHelper;
import com.communication.util.CommonUtils;

/**
 * Created by workEnlong on 2015/12/3.
 */
public class GpsBandCommandHelper extends BaseCommandHelper{


    public byte[] getIDCommand(){
        return getCommand(GpsBandConst.CODE_READ_ID);
    }

    public byte[] getBindCommand(){
        return getCommand(GpsBandConst.CODE_BIND);
    }

    public byte[] getVersionCommand(){
        return getCommand(GpsBandConst.CODE_VERSION);
    }

    /**
     * Query need write frame index
     * @param flag
     * @param
     * @return
     */
    public byte[] getWriteFileQueryCommand(String flag, String sname, int frame){

        byte[] name = CommonUtils.convertHexStringToByte(sname);

        byte[] content = new byte[1 + name.length ];
        content[0] = flag.getBytes()[0] ;
        for(int i = 0; i < name.length; i++){
            content[1 + i] = name[i];
        }
//        content[1 + name.length] = ( byte)((frame >> 16) & 0xff);
//        content[1 + name.length + 1] = ( byte)((frame >> 8) & 0xff);
//        content[1 + name.length + 2] = ( byte)((frame) & 0xff);

        return getCommand(GpsBandConst.CODE_PROGRESS_QUERY, content);
    }


    /**
     * write file content to device command
     * @param frame the index of content frame
     * @param data
     * @return
     */
    public byte[] getWriteFileContent(int frame, byte[] data){
        int value_length = 0;
        int check = 0;
        if(null != data ){
            value_length = data.length;
        }

        byte[] bytes = new byte[1 + 3 + value_length + 1];

        bytes[0] = (byte)((0xB0 & 0x0f0) + (value_length & 0x0f));
        bytes[1] = (byte)((frame >> 16) & 0xff );
        bytes[2] = (byte)((frame >> 8) & 0xff );
        bytes[3] = (byte)(frame  & 0xff );
        if(value_length >0){
            for (int i = 0; i < value_length; i++){
                bytes[4 + i] = data[i];
            }
        }

        for(byte v : bytes){
            check += v & 0xff;
        }

        bytes[bytes.length - 1] = (byte) (check & 0xff);

        return bytes;
    }
}
