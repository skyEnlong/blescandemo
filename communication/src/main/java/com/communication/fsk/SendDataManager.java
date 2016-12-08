package com.communication.fsk;

import java.util.ArrayList;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

public class SendDataManager implements CstCode {

	private final String TAG = SendDataManager.class.toString();
	short[] tempBuffer;

	private AudioTrack trackInstance = null;
	private FSKEncoding mEncoding;
	private boolean startKeepwrite = true;

	public SendDataManager() {
		initAudio();
		mEncoding = new FSKEncoding();
		mEncoding.setZeroFrequency(11.025f);
		mEncoding.setOneFrequency(7.35f);
		// keepWrite();
	}

	/**
     * 
     */
	public void writeHead() {
		short[] ones = mEncoding.getOne();
		for (int i = 0; i < 50; i++) {  //这里为什么是50
			try {
				if (trackInstance != null) {
					trackInstance.write(ones, 0, ones.length);
					trackInstance.flush();
				} else {
					Log.d(TAG, "trackInstance is null");
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

	}

	/**
	 * 
	 * @param datas
	 */
	private void writeShorts(short number) {
		ArrayList<short[]> list = mEncoding.getANumber(number);

		for (short[] datas : list) {
			try {
				if (trackInstance != null) {
					trackInstance.write(datas, 0, datas.length);
					trackInstance.flush();
					trackInstance.reloadStaticData();
				} else {
					Log.d(TAG, "trackInstance is null");
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * 
	 * @param writeData
	 */
	public void write(final int[] writeData) {
		// stopKeepWrite();
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
				writeHead();
				int length = writeData.length;
				for (int i = 0; i < length; i++) {
					writeShorts((short) writeData[i]);
				}
//			}
//		}).start();

		// keepWrite();
	}

	private void keepWrite() {
		startKeepwrite = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (startKeepwrite) {
					short[] ones = mEncoding.getOne();
					if (trackInstance != null) {
						trackInstance.write(ones, 0, ones.length);
						trackInstance.flush();
					}
				}
			}
		}).start();

	}

	private Handler mHandler = new Handler();

	private void stopKeepWrite() {
		startKeepwrite = false;
	}

	/**
     * 
     */
	private void initAudio() {
		Log.d(TAG, "initAudio");
		String model = android.os.Build.MODEL;
		int STREAM_TYPE = AUDIOTRACK_STREAM_TYPE;
		//GT-N7100T is NOTE II , SM-N9006 is Samsung NOTE 3
//		if (model.equals("GT-N7100T") ) {
//			STREAM_TYPE = AudioManager.STREAM_MUSIC;
//		}
		Log.d(TAG, "--------------" + model);
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIOTRACK_SIMPLES,
				AUDIOTRACK_CONFIG, AUDIO_ENCODING);

		trackInstance = new AudioTrack(STREAM_TYPE, AUDIOTRACK_SIMPLES,
				AUDIOTRACK_CONFIG, AUDIO_ENCODING, minBufferSize,
				AUDIOTRACK_MODE);

		trackInstance.setStereoVolume(0, 1);
		trackInstance.play();
	}

	
	public void reInitAudio() {
		Log.d(TAG, "reInitAudio");

		stopAudio();
		
		String model = android.os.Build.MODEL;
		int STREAM_TYPE =  AudioManager.STREAM_MUSIC;
		//GT-N7100T is NOTE II , SM-N9006 is Samsung NOTE 3
//		if (model.equals("GT-N7100T") ) {
//			STREAM_TYPE = AudioManager.STREAM_MUSIC;
//		}
		Log.d(TAG, "--------------" + model);
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIOTRACK_SIMPLES,
				AUDIOTRACK_CONFIG, AUDIO_ENCODING);

		trackInstance = new AudioTrack(STREAM_TYPE, AUDIOTRACK_SIMPLES,
				AUDIOTRACK_CONFIG, AUDIO_ENCODING, minBufferSize,
				AUDIOTRACK_MODE);

		trackInstance.setStereoVolume(0, 1);
		trackInstance.play();
	}
	/**
     * 
     */
	public void stopAudio() {
		Log.d(TAG, "stopAudio");
		if (trackInstance != null) {
			trackInstance.stop();
			trackInstance.release();
			trackInstance = null;
		}
	}
}
