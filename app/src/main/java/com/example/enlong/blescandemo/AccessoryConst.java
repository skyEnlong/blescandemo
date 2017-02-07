package com.example.enlong.blescandemo;

/**
 * Created by workEnlong on 2015/2/26.
 */
public interface AccessoryConst {

    ////////////////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////
    //             state of communication with accessory                               //
    /////////////////////////////////////////////////////////////////////////////////////
    public static final int STATE_BEGIN_CONNECT = 0xf0f0; // 开始连接设备
    public static final int STATE_SYNC_DATA_ING = 0x01; // 正在读取数据

    public static final int STATE_CONNECT_SUCESS = 0x02; // 连接成功
    public static final int STATE_GET_DEVICE_ID = 0x03; // 连接读取ID
    public static final int STATE_GET_VERSION = 0x04; // 连接读取version
    public static final int STATE_BIND_SUCESS = 0x12; // 绑定成功

    public static final int STATE_GET_BUTTERY = 0x08; // 获取了电池信息

    public static final int STATE_CLEAR_DATA = 0x0c;  //擦除数据

    public static final int STATE_UPDATE_USER_INFO = 0x0a; // 更新用户信息
    public static final int STATE_UPDATE_CLOCK_INFO = 0x0b; // 更新闹钟信息

    public static final int STATE_GET_DATA_OVER = 0x05; // 同步数据结束
    public static final int STATE_SERVICE_BEGIN = 0x09; // 上传数据
    public static final int STATE_SERVICE_SHOSE_BEGIN = 0x1009; // 上传shose数据



    public static final int STATE_SERVICE_FAILED = 0x06; // 上传数据失败
    public static final int STATE_SERVICE_SUCCESS = 0x07; // 上传数据成功
    public static final int STATE_DEVICE_CONNECT_CHANGE = 0x51; // 心率设备连接变化
    public static final int MSG_FOUND_BLE_DEVICE = 0x21;
    public static final int MSG_SYNC_TIMEOUT = 0xff; // 同步数据超时

    public static final int MSG_SEARCH_TIME_OUT = 0x22;
    public static final int MSG_NOT_INSERT_DEVICE = 0xfe; // 没有插入耳机
    public static final int MSG_NOT_SUPPORT = 0xfd; // 系统不支持BLE
    public static final int MSG_CONNECT_INTERRUPT = 0xfc; // 连接中断
    public static final int MSG_NOT_MATCH = 0xfb; // 设备不匹配
    public static final int MSG_DEVICE_BUSY = 0x24;   // 状态忙


    public static final int MSG_HAS_BOUND_JIEDE = 0x27;    // 已经绑定过捷德
    public static final int MSG_SYNC_START = 0x25;    // 开始同步
    public static final int MSG_GET_HEART = 0x23;
    public static final int MSG_QUIT_SYNC = 0x26;   // 放弃同步


    public static final int UPGRADE_DEVICE_ING = 0xE1; // 正在升级
    public static final int UPGRADE_DEVICE_RESULT = 0xE2; // 升级结果
    public static final int UPGRADE_CHANGE_BOOT_MODE = 0xE3;   //转换到boot模式
    public static final int UPGRADE_CONNECT_BOOT_MODE = 0xE4;   //连接到boot模式
    public static final int UPGRADE_BOOT_VERSION = 0xE5;        //boot模式下的version


    public static final int MSG_UNION_BIND_PIN = 0x1111;  // 银联手环绑定pin码


    /**
     * 获取体重信息
     */
    public static final int SYNCDATA_GET_WEIGHT = 0x30;

    /**
     * 蓝牙交友 震动提示
     */
    public static final int MSG_FRIENDS_WARING_OVER = 0x31;     // friends warning over

    /**
     * friends switch set over
     */
    public static final int MSG_FRIENDS_SET_OVER = 0x32;     //


    /**
     * seartch next device
     */
    public static final int MSG_SERTCH_NEXT_DEVICE = 0x33;     //


    /**
     * message of get gps band info
     */
    public static final int MSG_GET_GPS_INFO = 0x34;     //


    public static final int MSG_GET_DEVICE_EXPRESSION = 0x35;     //

    /////////////////////////////////////////////////////////////////////////////////////
    //             action of communication with accessory                               //
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 解绑设备
     */
    public static final int ACTION_UNBIND = -2;


    public static final int ACTION_NONE = -1;

    /**
     * 绑定设备
     */
    public static final int ACTION_BIND = 1;

    /**
     * 同步数据
     */
    public static final int ACTION_SYNC_DATA = 2;

    /**
     * 固件升级
     */
    public static final int ACTION_UPGRADE = 101;


    /**
     * 更新活动提醒
     */
    public static final int ACTION_UPDATE_CLOCK = 3;

    /**
     * 更新智能闹钟
     */
    public static final int ACTION_UPDATE_ALARM = 3;


    /**
     * 更新目标值
     */
    public static final int ACTION_UPDATE_TARGET = 4;
    public static final int ACTION_UPDATE_USER_INFO = 4;

    /**
     * 获取目标值
     */
    public static final int ACTION_GET_TARGET = 6;


    /**
     * 获取电量
     */
    public static final int ACTION_GET_BATTERY = 7;


    /**
     * 获取设备信息
     */
    public static final int ACTION_GET_DEVICE_INFO = 8;


    /**
     * 获取设备消耗信息
     */
    public static final int ACTION_GET_DEVICE_COST_INFO = 9;


    /**
     * friends warning
     */
    public static final int ACTION_FRIENDS_WARNING = 10;

    /**
     * friend turn
     */
    public static final int ACTION_FRIENDS_TURN = 11;


    ///////////////////////////////////////////////////////////////////
    ///---------------------------------------------------------------//
    ///////////////////////////////////////////////////////////////////
    public static final int MSG_CONNECT_BLE = 0xf1f1;
    public static final int MSG_UPGRADE_BLE = 0xf1f2;


    ///////////////////////////////////////////////////////////////////
    //----------------------------------------------------------------//
    ///////////////////////////////////////////////////////////////////
    public static final String EXTRA_DEVICE_CONFIG = "config";
    public static final String EXTRA_DEVICE_IDENTITY = "config_identity";


    public static final String EXTRA_SOURCE_FROM = "from_health";
    public static final String SOURCE_DEVICE = "device";
    public static final String SOURCE_HEALTH = "health";

    ///////////////////////////////////////////////////////////////////
    //----------------------------------------------------------------//
    ///////////////////////////////////////////////////////////////////
    public static final int RESULT_UNBIND = 0x0f;


    ///////////////////////////////////////////////////////////////////
    //----------------------------------------------------------------//
    ///////////////////////////////////////////////////////////////////
    public static final int TARGET_CALORIE = 0;
    public static final int TARGET_STEP = 1;
    public static final int TARGET_DISTANCE = 2;
    public static final int TARGET_SLEEP = 3;
    public static final int TARGET_WEIGHT = 4;
    public static final int TARGET_HEART = 4;



    ///////////////////////////////////////////////////////////////////
    //----------------------------------------------------------------//
    ///////////////////////////////////////////////////////////////////

    public static final String DEVICE_TYPE_ROM = "rom_device";
    public static final String DEVICE_TYPE_STEP = "step_trace";
    public static final String DEVICE_TYPE_WEIGHT = "weight";
    public static final String DEVICE_TYPE_WEAR = "android_wear";
    public static final String DEVICE_TYPE_HEART = "heart_rate_sensor";
    public static final String DEVICE_TYPE_SHOSE = "shose";
    public static final String DEVICE_TYPE_GPS = "gps_band";


    public static final String DEVICE_NAME_JABRA = "Jabra";
    public static final  String DEVICE_NAME_HEART_SENSOR = "Heartrate Sensor";
    public static final String DEVICE_NAME_ZTE_GUARD = "Goblin";
    public static final String DEVICE_NAME_STCBL = "STCBL";
    public static final String DEVICE_NAME_SHOSE_TEBU = "COD_TB";

    //产品简写
    public static final String CONDOON_SMILE_DEVICENAME = "CSL";// 咕咚笑
    public static final String CONDOON_CANDY_DEVICENAME = "CANDY";  //咕咚糖果
    public static final String CONDOON_SMARTBAND_DEVICENAME = "CSBS";   //咕咚手环s
    public static final String CONDOON_BLEBRACELET_DEVICENAME = "CBL";// 蓝牙手环2
    public static final String CONDOON_CODOON_DEVICENAME = "codoon";// 微信手环
    public static final String CONDOON_ZNWB_DEVICENAME = "Aster";// 智能腕表

    public static final String CONDOON_ZTE_BAND_NAME = "ZTECBL";
    public static final String CONDOON_MTK_BAND_NAME = "Aster";


    public static final String CONDOON_LENOVO_BAND_NAME = "Smartband";
    public static final String WEIGHT_SCALE_NAME = "COD_WXC";         // 脂肪秤

    /**
     * union pay band for jiede
     */
    public static final String DEVICE_NAME_UNIONPAY_JIEDE = "UPWEAR";

    //name for gps band
    public static final String DEVICE_NAME_GPS_BAND = "G1";
    public static final String DEVICE_NAME_GPS_OTHER = "GPS_BAND_OTHER";
    /////////////////////////////////////////////////////////////////
    //--------------------配件的功能类型描述-----------------------------------//
    ////////////////////////////////////////////////////////////////////

    public static final int FUNCTION_STEP = 0x01;  // step
    public static final int FUNCTION_SLEEP = 0x02;  // sleep
    public static final int FUNCTION_HEART = 0x04;  // heart rate sensor
    public static final int FUNCTION_WEIGHT = 0x08;  // fat weight scale

    public static final int FUNCTION_GPS = 0x20;  // gps

    public static final int FUNCTION_PAY = 0x10;  // union1


    public static final int FUNCTION_NO_UNION = 0xff00;
    public static final int FUNCTION_UNION_NO_UP = 0xff01;
    public static final int FUNCTION_UNION_2 = 0xff02;


    public static final int FUNCTION_NONE = 0x00;
    public static final int FUNCTION_ALL = 0xff;   // has all function

}