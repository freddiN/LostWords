package de.freddi.android.lostwords;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

/**
 * Created by Boris on 18.04.2016.
 */
class TTSAudioManagerListener extends UtteranceProgressListener {
    private final AudioManager m_audioManager;
    private final AudioManager.OnAudioFocusChangeListener m_audioFocusListener = new AudioFocusListener();
    private final TextToSpeech m_tts;

    public TTSAudioManagerListener(Context ctx, TextToSpeech tts) {
        m_audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        m_tts = tts;
    }

    @Override
    public void onStart(String utteranceId) {
        final int audioFocus = m_audioManager.requestAudioFocus(m_audioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            stopAndClearTTSQueue();
        }
    }

    @Override
    public void onDone(final String utteranceId) {
        abandonAudioFocus();
    }

    @Override
    public void onError(final String utteranceId) {
        abandonAudioFocus();
    }

    private void abandonAudioFocus() {
        m_audioManager.abandonAudioFocus(m_audioFocusListener);
    }

    private void stopAndClearTTSQueue() {
        if (m_tts != null) {
            m_tts.stop();
        }
    }

    private final class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // if the audiofocus is lost,
                    stopAndClearTTSQueue();
                    break;
            }
        }
    }
}
