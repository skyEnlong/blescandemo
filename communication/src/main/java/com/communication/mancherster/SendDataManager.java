package com.communication.mancherster;

import java.util.ArrayList;

import android.media.AudioManager;
import android.media.AudioTrack;

public class SendDataManager implements CstCode {

	private AudioTrack trackInstance = null;

	public SendDataManager() {
		initAudio();
	}

	/**
     * 
     */
	private void initAudio() {

		int minBufferSize = AudioTrack.getMinBufferSize(AUDIOTRACK_SIMPLES,
				AUDIOTRACK_CONFIG, AUDIO_ENCODING);

		trackInstance = new AudioTrack(AUDIOTRACK_STREAM_TYPE,
				AUDIOTRACK_SIMPLES, AUDIOTRACK_CONFIG, AUDIO_ENCODING,
				minBufferSize, AUDIOTRACK_MODE);

		trackInstance.play();
		trackInstance.setStereoVolume(1, 1);
	}

	public void reInitAudio(){
		
		stopAudio();
		
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIOTRACK_SIMPLES,
				AUDIOTRACK_CONFIG, AUDIO_ENCODING);

		trackInstance = new AudioTrack( AudioManager.STREAM_SYSTEM,
				AUDIOTRACK_SIMPLES, AUDIOTRACK_CONFIG, AUDIO_ENCODING,
				minBufferSize, AUDIOTRACK_MODE);

		trackInstance.play();
		trackInstance.setStereoVolume(1, 1);
	}
	
	
	public void resetEncoding(boolean flg) {
		ManchesterEncoding.resetHighLowBit(flg);
	}

	/**
	 * Handler is passed to pass messages to main screen Recording is done
	 * 8000Hz MONO 16 bit
	 */
	public void write(int[] writeData) {
		try {
			ArrayList<short[]> list = new ArrayList<short[]>();

			ManchesterEncoding encoding = new ManchesterEncoding();

			ArrayList<ArrayList<short[]>> lstbuffer = new ArrayList<ArrayList<short[]>>();

			for (int i = 0; i < writeData.length; i++) {
				lstbuffer.add(encoding.getManchesterCode(writeData[i]));
			}

			// head
			ArrayList<short[]> lstStart = encoding.getStart();
			for (int j = 0; j < lstStart.size(); j++) {
				if (trackInstance != null) {

					trackInstance.write(lstStart.get(j), 0, ARR_LEN_SIMPLES);
				}
			}

			// content
			for (int i = 0; i < writeData.length; i++) {
				list = lstbuffer.get(i);
				for (int j = 0; j < list.size(); j++) {
					if (trackInstance != null) {

						trackInstance.write(list.get(j), 0, ARR_LEN_SIMPLES);
					}
				}
			}
			if (trackInstance != null) {
				trackInstance.flush();
				trackInstance.reloadStaticData();
			}
		} catch (Exception e) {

		}

	}

	/**
     * 
     */
	public void stopAudio() {
		if (trackInstance != null) {
			trackInstance.stop();
			// trackInstance.flush();
			trackInstance.release();
			// trackInstance.release();
			trackInstance = null;
		}
	}
}
