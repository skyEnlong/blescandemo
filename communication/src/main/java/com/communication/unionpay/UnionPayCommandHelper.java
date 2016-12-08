package com.communication.unionpay;

import android.content.Context;
import android.text.TextUtils;
  
import com.communication.data.AccessoryConfig;
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.data.SLIPUtil;
import com.communication.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by workEnlong on 2015/6/11.
 */
public class UnionPayCommandHelper {

	/**
	 * 序号 偏移 字节长度 含义 存在方式 1 0 1 工作模式 必须 2 1 1 保留字节 必须 3 2 2 发送序列计数器SSC 必须 4 4 2
	 * 命令码 必须 5 6 2 命令长度 必须 6 8 可变 APDU 条件存在 7 Xx 1 字节校验和（LRC） 必须
	 * 对从序号为1～4的数据逐字节异或，结果作为整个数据的校验数据。
	 */
	private static final byte mode = 1;
	public static final String KEY_AUTH_CODE = "key_jiede_auth_code";
	private static final byte rfu = 0;
	private int ssc = 0;
	private int cmd = 0;
	private int command_len;
	private int check;
	private byte[] holeCommand = null;

	private int frameCount = 0;
	private static final int EACH_FRAME_LEN = 20;

	public UnionPayCommandHelper() {

	}
	
	public void setCommand(int ssc, int cmd, byte[] data) {
		this.ssc = ssc & 0xffff;
		this.cmd = cmd & 0xffff;

		command_len = (null == data) ? 0 : data.length;
		check = mode ^ rfu ^ ((ssc >> 8) & 0xff) ^ (ssc & 0xff)
				^ ((cmd >> 8) & 0xff) ^ (cmd & 0xff);

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(mode);
			os.write(rfu);
			os.write((byte) ((this.ssc >> 8) & 0xff));
			os.write((byte) (this.ssc & 0xff));
			os.write((byte) ((this.cmd >> 8) & 0xff));
			os.write((byte) (this.cmd & 0xff));
			os.write((byte) ((command_len >> 8) & 0xff));
			os.write((byte) (command_len & 0xff));
			if (null != data && data.length > 0) {
				os.write(data, 0, data.length);
			}
			os.write((byte) (check & 0xff));

			holeCommand = SLIPUtil.encode(os.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null != holeCommand){
			if(holeCommand.length % EACH_FRAME_LEN != 0){
				frameCount = holeCommand.length
						/ EACH_FRAME_LEN + 1;
			}else{
				frameCount = holeCommand.length
				/ EACH_FRAME_LEN;
			}
		}else{
			frameCount = 0;
		}
		CLog.i("union_pay", "command frame count == " + frameCount);
		DataUtil.DebugPrint("union_pay_hole_command:", holeCommand);

	}

	public int getFrameCount() {
		return frameCount;
	}

	public byte[] getFrameByIndex(int index) {
		if (index < 0 || index >= frameCount)
			return null;
		byte[] outPut = null;
		if ((index + 1) == frameCount) {
			outPut = Arrays.copyOfRange(holeCommand, index * EACH_FRAME_LEN,
					holeCommand.length);
		} else {
			outPut = Arrays.copyOfRange(holeCommand, index * EACH_FRAME_LEN,
					(index + 1) * EACH_FRAME_LEN);

		}

		return outPut;
	}

	/**
	 * @param pinByte
	 *            length 6
	 * @return
	 */
	public synchronized static byte[] getBindOrderData(Context mContext, byte[] pinByte) {
		byte[] data = new byte[55];
		data[0] = 0x02;
		for (int i = 1; i < 7; i++) { // pin length:6
			data[i] = (byte) (('0' + pinByte[i - 1]) & 0xff);
		}
		Random random = new Random();
		byte[] value_code = null;

		String uid = AccessoryConfig.getStringValue(mContext, KEY_AUTH_CODE);
		if (!TextUtils.isEmpty(uid)) {
			CLog.i("union_pay", "auth has:" + uid);

			value_code = CommonUtils.convertHexStringToByte(uid);

		} else {
			value_code = new byte[16];
			for (int i = 0; i < 16; i++) { // RFU length 48
				value_code[i] = (byte) (random.nextInt(255) & 0xff);
			}
			String str = CommonUtils.convertByteToHexString(value_code);
			AccessoryConfig.setStringValue(mContext, KEY_AUTH_CODE, str);
			CLog.i("union_pay", "auth null:" + str);
		}

		for (int i = 7; i < 23; i++) { // RFU length 48
			data[i] = value_code[i - 7];
		}

		for (int i = 23; i < 55; i++) {
			data[i] = 0;
		}
		return data;
	}

	/**
	 * @param pinByte
	 *            length 6
	 * @return
	 */
	public byte[] getUnBindOrderData(byte[] pinByte) {
		byte[] data = new byte[55];
		data[0] = 0x02;
		for (int i = 1; i < 7; i++) { // pin length:6
			data[i] = pinByte[i - 1];
		}

		for (int i = 7; i < data.length; i++) { // RFU length 48
			data[i] = 0;
		}
		return data;
	}

	/**
	 * 打开逻辑通道
	 * 
	 * @param cmd
	 * @return
	 */
	public static byte[] getOpenLogicCmd(byte[] cmd) {
		byte[] data = new byte[5 + cmd.length];
		data[0] = 0x00;
		data[1] = (byte) (0xa4 & 0xff);
		data[2] = (byte) (0x04 & 0xff);
		data[3] = 0x00;
		data[4] = (byte) (cmd.length & 0xff);
		for (int i = 0; i < cmd.length; i++) {
			data[5 + i] = (byte) (cmd[i] & 0xff);
		}

		DataUtil.DebugPrint("union_pay_select", data);
		return data;
	}
	
	

	/*
	 *		80CA  9F7F 2D
	 * tag: 9F7F  len: 2D
	 */
	public static byte[] getCPLCCmd() {
		byte[] data = new byte[] { (byte) (0x80 & 0xff), (byte) (0xCA & 0xff),
				(byte) (0x9F & 0xff), (byte) (0x7F & 0xff),
				(byte) (0x2D & 0xff) };
		return data;
	}
	
	/*
	 *		00C00000tt
	 * tag: 00 len: tt
	 */
	public static byte[] getCPLCCmd2(byte tt) {
		byte[] data = new byte[] { (byte) (0x00 & 0xff), (byte) (0xC0 & 0xff),
				(byte) (0x00 & 0xff), (byte) (0x00 & 0xff),
				(byte) tt };
		return data;
	}
}
