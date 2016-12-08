package com.communication.fsk;

public class FSKDecoding {
	private IFSKNumberCallback mCallback;
	private int THRESHOLD = 100;
	private int uartByte = 0;
	private int bitNum = 0;
	private int parityRx = 0;

	private int TIME = 0;

	private final int CHECK = 0, STARTBIT = 1, DECODE = 2;
	private int decState = CHECK;

	private boolean isFall = true;

	/**
	 * 
	 * @param fskNumberCallback
	 */
	public FSKDecoding(IFSKNumberCallback fskNumberCallback) {
		mCallback = fskNumberCallback;
	}

	int length = 48;
	short[] buf = new short[length];
	short max = 0;
	int cur = -1;

	/**
	 * 
	 * Functioin: decoding AudioRecord data
	 * @param buffer ： Data from AudioRecord
	 * @param bufferLength
	 */
	public void decoding(short[] buffer, int bufferLength) {
		for (int i = 0; i < bufferLength; i++) {
			TIME++;
			int cycle = getCycle(buf);//计算周期
			buf[47] = buffer[i];

			if (cycle >= 11 && cycle <= 13) {// 12��11025Hz���Ҳ���ʾ0
				cur = 0;
			} else if (cycle >= 7 && cycle <= 9) {// 8��7350Hz���Ҳ���ʾ1
				cur = 1;
			}
			switch (decState) {
			case CHECK:
				if (cur == 1) {
					decState = STARTBIT;
				}
				break;
			case STARTBIT:
				if (cur == 0) {
					decState = DECODE;
					TIME = (-12);
					cycle = 0;
					uartByte = 0;
					bitNum = 0;
					parityRx = 0;
				}

				break;
			case DECODE:
				if (TIME == 48) {
					TIME = 0;
					int currentBit = cur;
					cycle = 0;
					if (bitNum < 8) {
						uartByte = (uartByte >> 1) + (currentBit << 7);
						bitNum++;
						parityRx += currentBit;
					} else if (bitNum == 8) {
						// parity bit
						if (currentBit != (parityRx & 0x01)) {
							decState = CHECK;
							cur = -1;
						} else {
							bitNum++;
						}
						// bitNum++;
					} else {
						// we should now have the stop bit
						if (currentBit == 1) {
							mCallback.getNumber(uartByte);
						}
						decState = CHECK;
						cur = -1;
					}
				}
				break;
			}

		}
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 */
	private int getCycle(short[] buffer) {
		int max = buffer[0];
		int cycle = 0;
		for (int i = 0; i < length; i++) {
			int val = buffer[i];
			if (isFall) {
				if (max > val) {
					max = val;
				} else if (max < val && (val > THRESHOLD)) {
					isFall = false;
					cycle++;
				}
			} else {
				if (max < val) {
					max = val;
				} else if (max > val && (val < THRESHOLD)) {
					isFall = true;
					cycle++;
				}
			}

			if (i < 47) {
				buffer[i] = buf[i + 1];
			}
		}
		return cycle / 2;
	}

	public void setThreshold(int threshold) {
		THRESHOLD = threshold;
	}

}
