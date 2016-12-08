package com.communication.gpsband;

import com.communication.common.BaseCommand;

/**
 * Created by workEnlong on 2015/12/3.
 */
public class GpsBandConst extends BaseCommand{

    /**
     * 密钥协商 0x0B 协商生成加解密密钥
     读取文件总数 0x0C 读取文件总数和数据量
     读取文件信息 0x0D 读取下控端的文件名和数据量
     选择文件 0x0E 选择上传的文件
     上传文件 0x0F 批量上传文件
     擦除数据 0x15 擦除同步过的数据文件
     文件校验 0x16 CRC16 校验传输后的文件
     升级固件 0x70 升级固件准备工作
     连接 BOOT 0x71 测试进入 BOOT 成功
     下载固件 主批量通道 下载固件程序
     主查询命令 0x73 用于上控端更新下控端固件
     */
    public static final int CODE_SECRET_KEY = 0x0B;
    public static final int CODE_FILE_COUNT = 0x0C;
    public static final int CODE_FILE_INFO = 0x0D;
    public static final int CODE_FILE_SELECT = 0x0E;
    public static final int CODE_FILE_UPLOAD = 0x0F;


    public static final int CODE_CLEAR_DATA = 0x15;
    public static final int CODE_FILE_CHECK = 0x16;


    public static final int CODE_UPGRADE_BOOT = 0x70;
    public static final int CODE_BOOT_CONNECT = 0x71;
    public static final int CODE_PROGRESS_QUERY = 0x73;
    public static final int CODE_PROGRESS_RESET = 0x74;

    public static final int RES_SECRET_KEY = 0x8B;
    public static final int RES_FILE_COUNT = 0x8C;
    public static final int RES_FILE_INFO = 0x8D;
    public static final int RES_FILE_SELECT = 0x8E;
    public static final int RES_FILE_UPLOAD = 0x8F;


    public static final int RES_CLEAR_DATA = 0x95;
    public static final int RES_FILE_CHECK = 0x96;


    public static final int RES_UPGRADE_BOOT = 0xF0;
    public static final int RES_BOOT_CONNECT = 0xF1;
    public static final int RES_PROGRESS_QUERY = 0xF3;
    public static final int RES_PROGRESS_RESET = 0xF4;

    public static final String FLAG_G = "G";    // Gps detail info
    public static final String FLAG_S = "S";  // Gps total info
    public static final String FLAG_N= "N";  // step info
    public static final String FLAG_E = "E"; // Ephemeris

}
