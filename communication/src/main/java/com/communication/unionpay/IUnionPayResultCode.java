package com.communication.unionpay;

/**
 * Created by workEnlong on 2015/6/15.
 */
public interface IUnionPayResultCode {
    /**
     * BTC_IO_OK	00h 00h	通讯正常
     BTC_ILLEAGAL_CMD	00h 01h	非法命令
     BTC_IO_TIMEOUT	00h 02h	通讯超时
     BTC_IO_ERROR	00h 03h	通讯失败
     APC_IO_BUSY	00h 04h	BTC忙
     BTC_ILLEAGAL_STATUS	00h 05h	非法状态
     BTC_SSC_ERROR	00h 06h	会话流水号异常
     BTC_MODE_ERROR	00h 07h	模式异常,发送指令与当前模式冲突BTC_IO_OK	00h 00h	通讯正常

     */
    public static final int BTC_IO_OK = 0x0000;

    public static final int BTC_ILLEGAL_CMD = 0x0001;

    public static final int BTC_IO_TIMEOUT = 0x0002;

    public static final int BTC_IO_ERROR = 0x0003;

    public static final int APC_IO_BUSY = 0x0004;

    public static final int BTC_ILLEGAL_STATUS = 0x0005;

    public static final int BTC_SSC_ERROR = 0x0006;

    public static final int BTC_MODE_ERROR = 0x0007;

}
