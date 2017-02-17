package com.communication.shoes;

import com.communication.common.BaseCommand;

/**
 * Created by enlong on 2016/12/7.
 */

public class CodoonShoesCommand extends BaseCommand {

    /**
     * 准备同步数据命令
     **/
    public static final int CODE_READY_DATA = 0x0D;
    public static final int RES_READY_DATA = 0x8D;

    /**
     * 加速度传感器标定
     **/
    public static final int CODE_ACCESSORY_BD = 0x20;
    public static final int RES_ACCESSORY_BD = 0xA0;

    /**
     * 开始跑步
     **/
    public static final int CODE_START_RUN = 0x64;
    public static final int RES_START_RUN = 0xE4;


    /**
     * 结束跑步
     **/
    public static final int CODE_STOP_RUN = 0x65;
    public static final int RES_STOP_RUN = 0xE5;


    /**
     * 读取咕咚智能鞋跑步数据帧数
     **/
    public static final int CODE_RUN_DATA_TOTAL_FRAME = 0x66;
    public static final int RES_RUN_DATA_TOTAL_FRAME = 0xE6;


    /**
     * 读取咕咚智能鞋跑步数据
     **/
    public static final int CODE_READ_RUN_FRAME = 0x67;


    /**
     * 读取当前状态
     **/
    public static final int CODE_SHOES_STAE = 0x68;
    public static final int RES_SHOES_STAE = 0xE8;


    /**
     * 读取总里程
     **/
    public static final int CODE_SHOES_TOTAL_RUN = 0x69;
    public static final int RES_SHOES_TOTAL_RUN = 0xE9;

    /**
     * 读取跑步状态
     **/
    public static final int CODE_RUN_STATE_DATA = 0x83;
    public static final int RES_RUN_STATE_DATA = 0x03;

    /**
     * 服务端发送跺脚次数数据
     */
    public static final int RES_STOMP_DATA = 0x89;
    /**
     * 客户端响应
     */
    public static final int RESP_STOMP_DATA = 0x09;

}
