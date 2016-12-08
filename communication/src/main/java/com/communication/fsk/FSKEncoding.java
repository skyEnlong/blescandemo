package com.communication.fsk;

import java.util.ArrayList;

public class FSKEncoding {
	private float ZERO_FREQUENCY = 11.025f;
	private float ONE_FREQUENCY = 7.35f;

	private final boolean isMono = false;
	private float angle = 0;

	public void setZeroFrequency(float frequency) {
		ZERO_FREQUENCY = ((frequency * 1000) / 22050);
		angle = 0;
	}

	public void setOneFrequency(float frequency) {
		ONE_FREQUENCY = ((frequency * 1000) / 22050);
		angle = 0;
	}

	public short[] getZero() {
		int length = 12;
		ArrayList<Short> list = new ArrayList<Short>();
		int cycle = 0;
		while (cycle < length) {
			double val = Math.sin(angle / 180.0f * Math.PI);
			list.add((short) (Short.MAX_VALUE * val));
			angle += (180 * ZERO_FREQUENCY);
			if (angle >= 360) {
				cycle++;
				angle -= 360;
			}
		}
		int size = list.size();
		short[] datas = null;
		if (isMono) {
			datas = new short[size];
			for (int i = 0; i < size; i++) {
				datas[i] = list.get(i);
			}
		} else {
			datas = new short[size * 2];
			for (int i = 0; i < size; i++) {
				datas[2 * i] = list.get(i);
				datas[(2 * i) + 1] -= list.get(i);
			}
		}

		return datas;
	}

	public short[] getOne() {
		int length = 8;

		ArrayList<Short> list = new ArrayList<Short>();
		int cycle = 0;
		while (cycle < length) {
			double val = Math.sin(angle / 180.0f * Math.PI);
			list.add((short) (Short.MAX_VALUE * val));
			angle += (180 * ONE_FREQUENCY);
			if (angle >= 360) {
				cycle++;
				angle -= 360;
			}
		}
		int size = list.size();
		short[] datas = null;
		if (isMono) {//单通道
			datas = new short[size];
			for (int i = 0; i < size; i++) {
				datas[i] = list.get(i);
			}
		} else {//双通道
			datas = new short[size * 2];
			for (int i = 0; i < size; i++) {
				datas[2 * i] = list.get(i);
				datas[(2 * i) + 1] -= (list.get(i));
			}
		}

		return datas;
	}

	/**
	 *  一共有11位，起始位，中间8位， 还有一共奇偶位， 最后两位结束符
	 * @param number
	 * @return
	 */
	public ArrayList<short[]> getANumber(short number) {
		// start bit 0
		// number 8 bit ,low at front,high at behind
		// odd or even verify 1bit , odd is 1,even 0
		// stop 2 bit ,bit 1 bit 1
		ArrayList<short[]> list = new ArrayList<short[]>();

		// start bit 0
		list.add(getZero());
		// number 8 bit
		byte oddEvenValue = 0;
		for (int i = 0; i < 8; i++) {
			int oddEven = number & 1;
			number = (short) (number >> 1);
			if (oddEven == 1) {
				oddEvenValue++;
				list.add(getOne());
			} else {
				list.add(getZero());
			}
		}

		// odd or even verify bit
		list.add(oddEvenValue % 2 == 1 ? getOne() : getZero());

		// stop 2 bit ,11
		list.add(getOne());
		list.add(getOne());
		
		// in order to suport htc so that add 4 one
		list.add(getOne());
		list.add(getOne());
		list.add(getOne());
		list.add(getOne());

		return list;

	}

	public void sendData(short[] datas) {

	}
}
