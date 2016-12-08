package com.communication.data;

import java.io.ByteArrayOutputStream;

/**
 * Created by workEnlong on 2015/6/10.
 *
 *
 * SLIP协议定义了两个特殊字符，分别为END（0xc0）和ESC（0xdb），分组传输方式如下：
 *  a)分组以一个END字符开始；
 *  b)如果数据报中某个字符为END，那么连续传输两个字节ESC和0xdc来取代它；
 * c)如果数据报中某个字符为ESC，那么连续传输两个字节ESC和0xdd来取代它；
 *  d)分组以一个END字符结束。
 *
 *
 *
 *
 */
public class SLIPUtil {
    public static final byte END = (byte) (0xc0 & 0xff);
    private static final byte ESC = (byte) (0xdb & 0xff);
    private static final byte ESC_END = (byte) (0xdc & 0xff);
    private static final byte ESC_ESC = (byte) (0xdd & 0xff);

    /**
     * @param input
     * @return
     */
    public static byte[] encode(byte[] input) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(END);  // Starting TAG
        for (byte b : input) {
            if (b == END) {       // replace end
                os.write(ESC);
                os.write(ESC_END);
            } else if (b == ESC) {   // replace esc
                os.write(ESC);
                os.write(ESC_ESC);
            } else {
                os.write(b);
            }
        }
        os.write(END);  // Ending TAG

        return os.toByteArray();
    }


    /**
     * decode whole data
     * @param input
     * @return
     */
    public static byte[] decode(byte[] input) {
    	
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int len = input.length;
        if (input[0] != END || input[len - 1] != END) {
            return null;
        }
        for (int i = 1; i < len - 1; i++) {
            if (input[i] == ESC) {
                if (i < len - 2 && input[i + 1] == ESC_END) {
                    os.write(END);
                    i++;
                } else if (i < len - 2 && input[i + 1] == ESC_ESC) {
                    os.write(ESC);
                    i++;
                } else {
                    return null;
                }
            } else {
                os.write(input[i]);
            }
        }
        return os.toByteArray();
    }
}
