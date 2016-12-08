package com.communication.unionpay;

 
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.data.SLIPUtil;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Created by workEnlong on 2015/6/10.
 */
public class UnionPayResponseHelper implements IUnionPayResultCode {

	private static final String TAG = "union_pay";
	public static final int DONE = 0;
	public static final int ERROR = 1;
	public static final int PENDING = 2;
	
	public static final int RESEND = -1;


	private int receiveState = PENDING;
	private ByteArrayOutputStream receiveStream;
	private static final int MAX_SSC = 4096;
	private int ssc = 0;
	private int status = 0;
	private byte[] data = null; // APDU module data

	private byte[] test_data = null; // APDU module data

	public static int dealSsc(int ssc) {
		int flow = ssc & 0x0FFF;
		flow = (flow + 1) % MAX_SSC;
		if (flow == 0) {
			flow = 1;
		}
		return ((ssc & 0xF000) | flow);
	}

	public interface OnResponseListener {
		/**
		 * 
		 * @param data
		 *            处理组装好的数据
		 * @param ssc
		 *            本次会话产生的ssc
		 * @param status
		 *            状态码
		 */
		public void onResponse(byte[] data, int ssc, int status);

		public void onErr(int err_code);
		
		public void onResend();

	}

	private OnResponseListener mListener;

	public UnionPayResponseHelper(OnResponseListener mListener) {
		receiveStream = new ByteArrayOutputStream();
		this.mListener = mListener;
	}

	public int dealResponseFrameData(byte[] response_frame_data) {
		int len = response_frame_data.length;
		// remove padding zeros and err data at the end of the frame
		// Log.i(TAG, "len:"+Integer.toString(len));
//		if(CLog.isDebug){
//			
//			if(null != test_data && test_data.length > 0){
//				boolean isEqueal = true;
//				if(test_data.length == len){
//					for(int i = 0; i < test_data.length; i++){
//						if(test_data[i] != response_frame_data[i]){
//							isEqueal = false;
//							break;
//						}
//					}
//				}else{
//					isEqueal = false;
//				}
//				if(isEqueal){
//					CLog.e(TAG, "receive double data");
//					return receiveState;
//				}
//			}
//			test_data = response_frame_data;
//		}
				
		if (len == 1) {
			CLog.i(TAG, "帧数据在1字节，作为帧末尾处理");
			if (receiveStream.size() == 0) {
				receiveStream.reset();
				receiveState = ERROR;
			} else {
				receiveStream.write(response_frame_data, 0, len);
				receiveState = parse(receiveStream) ;
				responseByState(receiveState);
			}
		} else if (response_frame_data[0] == SLIPUtil.END
				&& response_frame_data[len - 1] == SLIPUtil.END) { // 帧数据在20字节以下
			CLog.i(TAG, "帧数据在20字节以下，做完整包处理");
			receiveStream.reset();
			receiveStream.write(response_frame_data, 0, len);
			receiveState = parse(receiveStream) ;
			responseByState(receiveState);
		} else if (response_frame_data[0] == SLIPUtil.END) {// 头，没有尾
			CLog.i(TAG, "收到头");
			receiveStream.reset();
			receiveStream.write(response_frame_data, 0, len);
			receiveState = PENDING;
		} else if (response_frame_data[len - 1] == SLIPUtil.END) {// 收到尾巴
			CLog.i(TAG, "收到尾巴");
			if (receiveStream.size() == 0) {
				receiveStream.reset();
				receiveState = ERROR;
			} else {
				receiveStream.write(response_frame_data, 0, len);
				receiveState = parse(receiveStream);
				responseByState(receiveState);
			}
		} else {// 中间数据
			CLog.i(TAG, "中间数据");
			if (receiveStream.size() == 0) {
				receiveStream.reset();
				receiveState = ERROR;
			} else {
				receiveStream.write(response_frame_data, 0, len);
				receiveState = PENDING;
			}
		}

		return receiveState;
	}

	public void clear() {
		receiveStream.reset();
		receiveState = PENDING;
		ssc = 0;
		status = 0;
		data = null;
	}

	public void responseByState(int state) {
		if (null == mListener)
			return;

		switch (state) {
		case DONE:
			mListener.onResponse(data, ssc, status);// UnionPayDeviceSyncManager中
			break;
		case ERROR:
			mListener.onErr(BTC_ILLEGAL_CMD);
			break;
		case RESEND:
			mListener.onResend();
			break;
		}
	}

	private int parse(ByteArrayOutputStream stream) {
		
		byte[] hole_res = stream.toByteArray();
		
		byte[] byteArray = SLIPUtil.decode(hole_res);
		if (byteArray == null) {
			return ERROR;
		}

		int len = byteArray.length;


		DataUtil.DebugPrint("union_pay_res", hole_res);
		
		if (len < 9) {
			CLog.e(TAG, "length not right--" + " protocol length:" + len);
			return ERROR;
		}

		if(isResend(byteArray)){
			return RESEND;
		}
		
		int check = (byteArray[0] ^ byteArray[1] ^ byteArray[2] ^ byteArray[3]
				^ byteArray[4] ^ byteArray[5]) & 0xff;

		// Log.i(TAG, "校验值："+Integer.toHexString(check));
//		if ((byteArray[len - 1] & 0xff) != check) {
//			CLog.e(TAG, "check not right--" + " protocol:"
//					+ (byteArray[len - 1] & 0xff) + " cal is:" + check);
//			return false;
//		}

		this.ssc = ((byteArray[2] << 8) & 0xff00) + (byteArray[3] & 0xff);
		this.status = ((byteArray[4] << 8) & 0xff00) + (byteArray[5] & 0xff);
		int data_len = ((byteArray[6] << 8) & 0xff00) + (byteArray[7] & 0xff);

		if (data_len + 9 != len) {
			CLog.e(TAG, "length not right--" + " protocol length:" + (data_len + 9)
					+ " real:" +  len);
			return ERROR;
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(byteArray, 8, data_len);
		data = os.toByteArray();
 		return DONE;
	}
	
	public boolean isResend(byte[] data){
		byte[] err = new byte[]{  1  , 0  , 0   ,0   ,0  , 1  , 0  , 0   ,0 };
		if(data.length == err.length){
			for(int i = 0; i < data.length; i++){
				if(data[i] != err[i]){
					return false;
				}
			}
			
			return true;
		} 
		
		return false;
	}
	
	public static byte[] parseCPLCInfo(byte[] data) {
		// TODO Auto-generated method stub
		if(null == data || data.length < 3) return null;
		byte[] cplcInfo = null;
		int len =  data[2];
		cplcInfo = Arrays.copyOfRange(data, 3, 3 + len);
 		return cplcInfo;
	}
}
