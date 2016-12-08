package com.communication.util;

/**
 * Created by workEnlong on 2015/6/11.
 */
public class UnionPayConstant {
    public static final String ACTION_CODOONPAY_SERVICE = "com.communication.unionpay.CodPayService";

    public class ResultCodeForUnionPay {
        public static final int SUCCESS = 0x0000;
        public static final int ERR_BLE = 0xFF00;
        public static final int ERR_SE = 0xFF01;
        public static final int ERR_CHANNEL = 0xFF02;
    }

    public class UnionBundleKey{
        public static final String KEY_COMMAND_TYPE = "commandType";
        public static final String KEY_RESULT_CODE = "resultCode";
        public static final String KEY_RESULT = "result";
        public static final String KEY_CHANNEL = "channel";

    }

    public class CommandType {
        public static final int OPEN_CHANNEL = 1;
        public static final int CLOSE_CHANNEL = 2;
        public static final int TRANSPARENT = 3;

    }


}
