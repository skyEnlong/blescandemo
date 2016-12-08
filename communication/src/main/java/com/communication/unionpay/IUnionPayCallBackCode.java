package com.communication.unionpay;

/**
 * Created by workEnlong on 2015/6/16.
 */
public interface IUnionPayCallBackCode {
    public static final int CALLBACK_INIT = 1000;
    public static final int CALLBACK_GET_ASSOCIATEDAPP = CALLBACK_INIT + 1;
    public static final int CALLBACK_GET_TRANSELEMENTS = CALLBACK_INIT + 2;
    public static final int CALLBACK_GET_SMSAUTHCODE = CALLBACK_INIT + 3;
    public static final int CALLBACK_GET_ACCOUNTINFO = CALLBACK_INIT + 4;
    public static final int CALLBACK_GET_ACCOUNTBALANCE = CALLBACK_INIT + 5;
    public static final int CALLBACK_APP_DOWNLOADAPPLY = CALLBACK_INIT + 6;
    public static final int CALLBACK_APP_DOWNLOAD = CALLBACK_INIT + 7;
    public static final int CALLBACK_APP_DELETE = CALLBACK_INIT + 8;
    public static final int CALLBACK_APP_LIST = CALLBACK_INIT + 9;
    public static final int CALLBACK_APP_DETAIL = CALLBACK_INIT + 10;
    public static final int CALLBACK_OPEN_CHANNEL = CALLBACK_INIT + 11;
    public static final int CALLBACK_SEND_APDU = CALLBACK_INIT + 12;
    public static final int CALLBACK_CLOSE_CHANNEL = CALLBACK_INIT + 13;
    public static final int CALLBACK_GET_SEAPP_LIST = CALLBACK_INIT + 14;
    public static final int CALLBACK_GET_DEFAULT_CARD = CALLBACK_INIT + 15;
    public static final int CALLBACK_SET_DEFAULT_CARD = CALLBACK_INIT + 16;
    public static final int CALLBACK_GET_TRANSRECORD = CALLBACK_INIT + 17;
    public static final int CALLBACK_ECASH_TOPUP = CALLBACK_INIT + 18;
    public static final int CALLBACK_GET_SE_ID = CALLBACK_INIT + 19;

}
