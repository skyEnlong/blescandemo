package com.communication.common;

/**
 * Created by workEnlong on 2016/1/21.
 */
public class BaseCommand {
    public static final int CODE_CONNECT = 0x01;
    public static final int RES_CONNECT = 0x81;

    /**获取设备类型和版本号**/
    public static final int CODE_VERSION = 0x02;
    public static final int RES_READ_VERSION = 0x82;

    /**获取设备ID　**/
    public static final int CODE_READ_ID = 0x04;
    public static final int RES_READ_ID = 0x84;

    /**更新设备时间**/
    public static final int CODE_UPDATE_TIME = 0x0A;
    public static final int RES_UPADTE_TIME = 0x8A;

    /**
     * 读取设备时间
     */
    public static final int CODE_GET_TIME = 0x0B;
    public static final int RES_GET_TIME = 0x8B;


    /**清除数据指令**/
    public static final int CODE_CLEAR_SPORT_DATA = 0x14;
    public static final int RES_CLEAR_SPORT_DATA = 0x94;

    /**更新用户信息**/
    public static final int CODE_UPDATE_USER_INFO = 0x05;
    public static final int RES_UPDATE_USER_INFO = 0x85;

    /**读取闹钟信息、电量**/
    public static final int CODE_GET_CLOCK = 0x08;
    public static final int RES_GET_CLOCK = 0x88;

    /**更新闹钟信息**/
    public static final int CODE_SET_CLOCK = 0x06;
    public static final int RES_SET_CLOCK = 0x86;

    /**读取运动数据帧数**/
    public static final int CODE_TOTAL_STEP_FRAME = 0x0C;
    public static final int RES_TOTAL_STEP_FRAME = 0x8C;

    /**读取运动数据**/
    public static final int CODE_READ_STEP_FRAME = 0x11;
    public static final int RES_READ_STEP_FRAME = 0x91;



    /** 绑定 **/
    public static final int CODE_BIND = 0x41;
    public static final int RES_BIND = 0xC1;


    public static final int CODE_GET_ORIGIN_DATA = 0x17;
    public static final int RES_GET_ORIGIN_DATA = 0x97;


}
