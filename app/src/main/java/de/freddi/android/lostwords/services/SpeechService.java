package de.freddi.android.lostwords.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import de.freddi.android.lostwords.Helper;
import de.freddi.android.lostwords.R;

/**
 * Created by freddi on 25.04.2016.
 */
public class SpeechService extends Service implements TextToSpeech.OnInitListener {
    public static final String EXTRA_PARAM = "PARAM";
    public static final String EXTRA_ACTION = "ACTION"; //siehe nachfolgende statics
    
    public static final String EXTRA_ACTION_SHUTDOWN = "SHUTDOWN";
    public static final String EXTRA_ACTION_SPEAK = "SPEAK";
    public static final String EXTRA_ACTION_CONFIGURE = "CONFIGURE";
    public static final String EXTRA_ACTION_TTSCOMMAND = "TTSCOMMAND";

    private TextToSpeech m_tts = null;
    private AtomicBoolean m_isInitialized = new AtomicBoolean(false);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (m_tts == null) {
            m_tts = new TextToSpeech(getApplicationContext(), this);
        }
        
        final String strAction = intent.getStringExtra(SpeechService.EXTRA_ACTION);
        final String strParam  = intent.getStringExtra(SpeechService.EXTRA_PARAM);

        if (EXTRA_ACTION_CONFIGURE.equals(strAction)) {
            doReconfigure();
        } else if (EXTRA_ACTION_SHUTDOWN.equals(strAction)) {
            doShutdown();
            stopSelf();
        } else if (m_isInitialized.get() && EXTRA_ACTION_SPEAK.equals(strAction)) {
            doSpeak(strParam);
        } else if (m_isInitialized.get() && EXTRA_ACTION_TTSCOMMAND.equals(strAction)) {
            doTTSCommand(strParam);
        } else {
            Helper.doLog("unbekannte Aktion=" + strAction + " m_isInitialized=" + m_isInitialized);
        }

        return SpeechService.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    private void doShutdown() {
        if (m_tts != null) {
            m_tts.stop();
            m_tts.shutdown();
            m_tts = null;
        }
    }
    
    private void doSpeak(final String strSpeakMe) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString());
        if (m_tts != null) {
            m_tts.speak(strSpeakMe, TextToSpeech.QUEUE_FLUSH, params);
        }
    }
    
    private void doReconfigure() {
        doShutdown();
        m_tts = new TextToSpeech(getApplicationContext(), this);
    }

    private void doTTSCommand(final String strCommand) {
        if (m_tts == null) {
            return;
        }
        
        if ("STOP".equalsIgnoreCase(strCommand)) {
            m_tts.stop();
        }
    }

    @Override
    public void onInit(int status) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String strLocale = preferences.getString(
                getResources().getString(R.string.settings_tts_locale),
                "");

        if(!TextUtils.isEmpty(strLocale) && status != TextToSpeech.ERROR && m_tts != null) {
            final int nResult = m_tts.setLanguage(new Locale(strLocale));
            if (nResult != TextToSpeech.SUCCESS) {
                m_isInitialized.set(false);
                doShutdown();
            } else {
                m_isInitialized.set(true);
                // TODO: Hmm, der stoppt jetzt regelmäggig die ausgabe wenn man n der app mehrfach drückt ... 
                //m_tts.setOnUtteranceProgressListener(new TTSAudioManagerListener(getApplicationContext(), m_tts));
            }
        } else {
            m_isInitialized.set(false);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
