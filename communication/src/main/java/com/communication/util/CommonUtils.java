package com.communication.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by workEnlong on 2015/6/12.
 */
public class CommonUtils {

	/**
	 * @param datas
	 * @return
	 */
	public static byte[] intToByte(int[] datas) {
		int size = datas.length;
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++) {
			bytes[i] = (byte) (datas[i] & 0x000000ff);
		}

		// for(int i = size; i < 20; i++){
		// bytes[i] = 0;
		// }
		return bytes;
	}

	/**
	 * change int to 2 byte
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] intoByte(int data) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((data >> 8) & 0xff);
		bytes[1] = (byte) (data & 0xff);
		return bytes;
	}

	public static String convertByteToHexString(byte[] data) {
		if (null == data || data.length == 0) {
			return null;
		}

		String str = "";
		for (int i = 0; i < data.length; i++) {
			String hexString = Integer.toHexString(data[i] & 0xff);
			if (hexString.length() == 1) {
				hexString = "0" + hexString;
			}
			str += hexString;
		}

		return str;
	}

	public static byte[] convertHexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toLowerCase().toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789abcdef".indexOf(c);
		return b;
	}

	public static String getHexString(byte data) {
		String str = Integer.toHexString(data & 0xff);
		if (str.length() == 1) {
			str = "0" + str;
		}
		return str;
	};


	public static String getHexString(int data) {
		String str = Integer.toHexString(data);
		if (str.length() == 1) {
			str = "0" + str;
		}
		return str;
	};

	public static ArrayList<Integer> changeArraytoList(byte[] bytes){
		if(null == bytes || bytes.length == 0) return null;
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (byte b : bytes){
			list.add(b & 0xff);
		}

		return list;
	}
	/**
	 * inner list length must be 6
	 * 
	 * @param data
	 * @return
	 */
	public static int[] changeListToArray(ArrayList<ArrayList<Integer>> data) {
		if (null == data || data.size() == 0) {
			return null;
		}

		int[] arr = new int[data.size() * data.get(0).size()];
		int index = 0;
		for (int i = 0; i < data.size(); i++) {
			List<Integer> list = data.get(i);
			for (int j = 0; j < list.size(); j++) {
				arr[index++] = list.get(j);
			}
		}

		return arr;
	}

	public static ArrayList<ArrayList<Integer>> changArrTo6List(int[] arr) {
		if (null == arr || arr.length == 0) {
			return null;
		}
		ArrayList<ArrayList<Integer>> lists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> list = null;
		for (int i = 0; i < arr.length; i++) {
			if (i % 6 == 0) {
				list = new ArrayList<Integer>();
				lists.add(list);
			}
			list.add(arr[i]);
		}

		return lists;
	}

}
