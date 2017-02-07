package com.communication.unionpay;

/**
 * Created by workEnlong on 2015/6/11.
 */
public interface UnionPayCommand {
    public final String TAG = "ble";
    /**
     * BTC_INFO	00h 01h	取蓝牙智能卡版本信息
     BTC_IDLE	00h 02h	设置安全芯片自动下电空闲时间
     BTC_UNIT	00h 03h	组件控制
     BTC_DATA	00h 04h	可穿戴组件数据透明传输
     BTC_AUTH	00h 05h	蓝牙连接认证
     BTC_DISCONNECT	01h 01h	对安全芯片的下电通知
     BTC_CONNECT	01h 02h	对安全芯片的上电通知
     BTC_ATR	01h 03h	通知 BTC 读取上一次复位中缓存的安全芯片的ATR信息
     BTC_APDU	01h 04h	通知BTC转发APDU命令
     BTC_PPS	01h 05h	PPS请求
     */


    public  final int BTC_INFO = 0x0001;

    public  final int BTC_IDLE = 0x0002;

    public  final int BTC_UNIT = 0x0003;

    public  final int BTC_DATA = 0x0004;

    public  final int BTC_AUTH = 0x0005;

    public  final int BTC_UNBIND = 0x0006;

    public final int BTC_STATUS = 0x0202;

    public  final int BTC_DISCONNECT = 0x0101;

    public  final int BTC_CONNECT = 0x0102;

    public  final int BTC_ATR = 0x0103;

    public  final int BTC_APDU = 0x0104;

    public  final int BTC_PPS = 0x0105;
    
    public  final int BTC_TEST = 0xE1E2;

}
