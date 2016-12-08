package com.communication.common;

/**
 * Created by workEnlong on 2016/1/21.
 */
public class BaseCommand {
    public static final int CODE_CONNECT = 0x01;

    /**获取设备类型和版本号**/
    public static final int CODE_VERSION = 0x02;

    public static final int RES_CONNECT = 0x81;


    /**更新设备时间**/
    public static final int CODE_UPDATE_TIME = 0x0A;
    public static final int CODE_CLEAR_SPORT_DATA = 0x14;

    /**读设备ID**/
    public static final int CODE_READ_ID = 0x04;
    public static final int CODE_BIND = 0x41;

    /**更新用户信息**/
    public static final int CODE_UPDATE_USER_INFO = 0x05;
    public static final int RES_UPDATE_USER_INFO = 0x85;

    /**读取运动数据帧数**/
    public static final int CODE_TOTAL_STEP_FRAME = 0x0C;
    public static final int RES_TOTAL_STEP_FRAME = 0x8C;

    /**读取运动数据**/
    public static final int CODE_READ_STEP_FRAME = 0x11;
    public static final int RES_READ_STEP_FRAME = 0x91;

    public static final int RES_READ_ID = 0x84;
    public static final int RES_READ_VERSION = 0x82;
    public static final int RES_BIND = 0xC1;
    public static final int RES_UPADTE_TIME = 0x8A;
    public static final int RES_CLEAR_SPORT_DATA = 0x94;
}
