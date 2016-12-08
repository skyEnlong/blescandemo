/*********************************************
 * ANDROID SOUND PRESSURE METER APPLICATION
 * DESC   : Recording Thread that calculates SPL.  
 * WEBSRC : Recording : http://www.anddev.org/viewtopic.php?p=22820
 * AUTHOR : hashir.mail@gmail.com
 * DATE   : 19 JUNE 2009
 * CHANGES: - Changed the recording logic
 * 			- Added logic to pass recorded buffer to FFT
 * 			- Added logic to calculate SPL.
 *********************************************/

package com.communication.fsk;

import android.media.AudioRecord;

public class Recorder implements Runnable, CstCode {

	private boolean isRecording = false;

	private boolean isOpen = false;

	private short[] tempBuffer;

	private AudioRecord recordInstance = null;

	// private HiJackDecoding decoding;

	// private FSKDecoding fskDecoding;
	private FSKDecoding fskDecoding;

	/**
	 * Handler is passed to pass messages to main screen Recording is done
	 * 8000Hz MONO 16 bit
	 */
	public Recorder(IFSKNumberCallback callback) {
		super();
		// decoding = new HiJackDecoding(mHandle);
		fskDecoding = new FSKDecoding(callback);
		// 鑾峰緱缂撳啿鍖哄瓧鑺傚ぇ灏�
		int bufferSize = AudioRecord.getMinBufferSize(AUDIORECORD_SIMPLE,
				AUDIORECORD_CONFIG, AUDIO_ENCODING);

		// AudioRecord
		recordInstance = new AudioRecord(AUDIORCORD_SOURCE, AUDIORECORD_SIMPLE,
				AUDIORECORD_CONFIG, AUDIO_ENCODING, bufferSize);

		// tempBuffer = new short[bufferSize > 2048 ? bufferSize >> 1 :
		// bufferSize];

		tempBuffer = new short[bufferSize];

		try {

			recordInstance.startRecording();

			new Thread(this).start();
		} catch (Exception e) {

		}

	}

	/**
	 * @param isRecording
	 *            the isRecording to set
	 */
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public void setThreshold(int threshold) {
		fskDecoding.setThreshold(threshold);
	}

	/* Recording THREAD */
	@Override
	public void run() {
		while (isRecording) {
			if (isOpen) {
				int bufferLength = recordInstance.read(tempBuffer, 0,
						tempBuffer.length);
				if (tempBuffer != null && tempBuffer.length > 0) {
					fskDecoding.decoding(tempBuffer, bufferLength);
				}
			}
		}

	}

	public void start() {
		isOpen = true;
	}

	/**
	 * start read data from device
	 */
	public void restart() {
		isOpen = true;
	}

	/**
	 * pause read data from device
	 */
	public void pause() {
		isOpen = false;
	}

	/**
     * 
     */
	public void stop() {
		setRecording(false);
		if (recordInstance != null) {
			try{
				
				recordInstance.stop();
				recordInstance.release();
			}catch(Exception e){
				
			}
			recordInstance = null;
		}
		isOpen = false;
	}

}
