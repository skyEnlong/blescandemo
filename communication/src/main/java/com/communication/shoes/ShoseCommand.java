package com.communication.shoes;

import com.communication.common.BaseCommand;

/**
 * Created by workEnlong on 2016/1/21.
 */
public class ShoseCommand extends BaseCommand{
    public static final int CODE_SHOES_SUMMARY = 0x60;
    public static final int CODE_SHOES_FRAME_DETAIL = 0x61;
    public static final int CODE_SHOES_DETAIL = 0x62;

    public static final int CODE_SHOES_CLEAR = 0x63;
    public static final int CODE_SHOES_COST = 0x64;   //total dis has use

    public static final int RES_SHOES_SUMMARY = 0xE0;
    public static final int RES_SHOES_DETAIL_FRAME = 0xE1;
    public static final int RES_SHOES_DETAIL = 0xE2;

    public static final int RES_SHOES_CLEAR = 0xE3;
    public static final int RES_SHOES_COST = 0xE4;

    //==========for boot up=========//
    public static final int CODE_BOOT_CHANGE = 0x70;
    public static final int CODE_BOOT_BEGIN_UPGRADE = 0x71;
    public static final int CODE_BOOT_TRANS_DATA = 0x73;
    public static final int CODE_BOOT_TRANS_CHECK = 0x74;

    public static final int RES_BOOT_CHANGE = 0xF0;
    public static final int RES_BOOT_BEGIN_UPGRADE = 0xF1;
    public static final int RES_BOOT_TRANS_DATA = 0xF3;
    public static final int RES_BOOT_TRANS_CHECK = 0xF4;

}
