package com.communication.mancherster;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public interface CstCode {
    public final short NUM_HIGH = 32767;
    public final short NUM_LOW = -32767;

    public final String ZERO = "10";
    public final String ONE = "01";

    //   11111
    // private final String START_STOP = "0101010101";
    public final String START_BIT = ZERO;
    public final String STOP_BIT = ONE;
    //  
    public final String VERIFY_BIT_EVEN = ZERO;
    //  
    public final String VERIFY_BIT_UNEVEN = ONE;

    // audio track stream type
   //  public final int AUDIOTRACK_STREAM_TYPE= MediaRecorder.AudioSource.MIC;
    public final int AUDIOTRACK_STREAM_TYPE=AudioManager.STREAM_MUSIC;
    // audio track mode
   // public final int AUDIOTRACK_MODE = AudioTrack.MODE_STREAM;
     public final int AUDIOTRACK_MODE=AudioTrack.MODE_STREAM;
    // Audio Track simple
    public final int AUDIOTRACK_SIMPLES = 44100;


    public final int ARR_LEN_SIMPLES=32;
    // audio track config
  //  public final int AUDIOTRACK_CONFIG=AudioFormat.CHANNEL_OUT_MONO;
    public final int AUDIOTRACK_CONFIG=AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    // audio format encoding
    public final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // audio record simple
    public final int AUDIORECORD_SIMPLE = 44100;
    // audio record config
    public final int AUDIORECORD_CONFIG = AudioFormat.CHANNEL_IN_DEFAULT; 
    // audio record source
    public final int AUDIORCORD_SOURCE = MediaRecorder.AudioSource.MIC;
}
