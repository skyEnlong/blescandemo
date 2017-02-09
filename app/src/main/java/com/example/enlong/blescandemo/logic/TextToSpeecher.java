package com.example.enlong.blescandemo.logic;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.Log;

import com.example.enlong.blescandemo.R;

import java.util.HashMap;
import java.util.Map;

public class TextToSpeecher {

    protected Map<SoundFactory, Integer> soundMap = new HashMap<SoundFactory, Integer>();
    public static TextToSpeecher mSoundPlayer;
    public Context mContext;
    public int currentVolume;
    public MediaPlayer player;
    public AudioManager audio;


    public synchronized static TextToSpeecher getInstance(Context context) {
        if (mSoundPlayer == null) {

            mSoundPlayer = new TextToSpeecher(context.getApplicationContext());

        }
        return mSoundPlayer;
    }

    public TextToSpeecher(Context context) {
        mContext = context;
        audio = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        loadVoice();
    }

    public void loadVoice() {

        soundMap.clear();
        //蓝牙断开
        soundMap.put(SoundFactory.BluetoothDisconnect, R.raw.bluetooth_disconnect);


    }


    private boolean isAdjust = false;


    private boolean isCurrentVolumeBig() {
        currentVolume = getCurrentVolume();
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if ((((float) currentVolume) / maxVolume) > 0.7f) {
            return true;
        }
        return false;
    }

    private int getCurrentVolume() {
        return audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void adjustVolume() {
//		int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		int curVolume = (int) (maxVolume * 0.7f);
//		audio.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume,
//		AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private void restoreVolume() {
        // audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume,
        // AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public void playSound(final SoundFactory soundFactory) {
        isAdjust = false;
        if (isCurrentVolumeBig()) {
            isAdjust = true;
            adjustVolume();
        }
        try {
            if (!requestFocus()) {
                return;
            }
            if (player != null && player.isPlaying()) {
                player.release();
                player = null;
            }
            player = createMediaPlayer(soundFactory);
            if (player == null) {
                return;
            }
            player.start();
            player.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    // player.reset();
                    if (player != null) {
                        player.release();
                    }
                    player = null;
                    if (isAdjust)
                        restoreVolume();
                    if (audio != null)
                        audio.abandonAudioFocus(afChangeListener);

                    // if (MusicService.getInstance() != null
                    // && !TooltipSportMusic.mIsPauseByPerson
                    // && soundFactory != SoundFactory.ContinueProgram
                    // && soundFactory != SoundFactory.Message) {
                    // MusicService.getInstance().startMusic();
                    // }
                }
            });
            player.setOnErrorListener(new OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                    // TODO Auto-generated method stub
                    if (player != null)
                        player.reset();
                    return false;
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
        }

    }


    protected int getDuration(int raw_id) {

        Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + raw_id);

        return getDuration(uri);
    }

    protected int getDuration(Uri uri) {
        MediaPlayer mediaPlayer = null;
        int duration = 0;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(mContext, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer = null;
        return duration;
    }



    private boolean requestFocus() {
        // Request audio focus for playback
        int result = audio.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                float volume = getCurrentVolume() * 0.5f;
                if (player != null)
                    player.setVolume(volume, volume);

                Log.d("kevin", "loss");

            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
                if (player != null)
                    player.setVolume(getCurrentVolume(), getCurrentVolume());
                Log.d("kevin", "focus");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                audio.abandonAudioFocus(afChangeListener);
                // Stop playback

            }

        }
    };


    /**
     * 创建player， 根据选择的语音包
     *
     * @param soundFactory
     * @return
     */
    private MediaPlayer createMediaPlayer(SoundFactory soundFactory) {
        if (null == soundFactory) return null;
        int resId = -1;
        Uri uri = null;

        MediaPlayer mediaPlayer = null;
        if (soundMap.containsKey(soundFactory)) {

            resId = soundMap.get(soundFactory);
        }

        mediaPlayer = MediaPlayer.create(mContext, resId);

        return mediaPlayer;
    }

    public static void destroy() {
        mSoundPlayer = null;
    }


    public void speechBluetoothLose() {
        playSound(SoundFactory.BluetoothDisconnect);
        return;
    }

}
