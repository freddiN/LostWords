package de.freddi.android.lostwords;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import de.freddi.android.lostwords.services.SpeechService;

/**
 * Created by Boris on 18.04.2016.
 */
public class TTSAudioManagerListener extends UtteranceProgressListener {
    private final AudioManager m_audioManager;
    private final AudioManager.OnAudioFocusChangeListener m_audioFocusListener = new AudioFocusListener();
    private final Context m_ctx;

    public TTSAudioManagerListener(Context ctx, TextToSpeech tts) {
        m_ctx = ctx;
        m_audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
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
        Helper.invokeSpeechService(SpeechService.EXTRA_ACTION_TTSCOMMAND, "stop", m_ctx);
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
