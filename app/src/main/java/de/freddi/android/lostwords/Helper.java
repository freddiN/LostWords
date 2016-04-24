package de.freddi.android.lostwords;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.SearchView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by freddi on 09.04.2016.
 * 
 * independent functionality
 */
public class Helper {

    /**
     * write to logcat, can be filtered by lookng for "LOSTWORDS"
     * 
     * @param strLogMe string to log
     */
    public static void doLog(final String strLogMe) {
        Log.d("LOSTWORDS", strLogMe);
    }

    /**
     * check if a touch event happened within a given list of buttons
     * 
     * @param pTouch coordinates of the touch
     * @param buttons list of buttons
     * @return true if touch fro within one of the buttons, false otherwise
     */
    public static boolean isTouchWithinButtons(final Point pTouch, final View... buttons) {
        int[] location = new int[2];
        boolean bXinButton, bYinButton;
        for (View button: buttons) {
            /** button position */
            button.getLocationInWindow(location);

            if (button instanceof SearchView) {
                /** ignore doubletaps on searchview */
                bYinButton = pTouch.y >= location[1] && pTouch.y <= location[1] + button.getHeight();
                return bYinButton;
            } else {
                /** touch within button ? */
                bXinButton = pTouch.x >= location[0] && pTouch.x <= location[0] + button.getWidth();
                bYinButton = pTouch.y >= location[1] && pTouch.y <= location[1] + button.getHeight();
                if (bXinButton && bYinButton) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * displays a snackbar at the bottom
     * 
     * @param strText text to display
     * @param v view to use
     * @param nDuration e.g. Snackbar.LENGTH_SHORT
     */
    public static void showSnackbar(final String strText, final View v, final int nDuration) {
        if (v != null) {
            Snackbar.make(v, strText, nDuration).show();
        }
    }

    /**
     * closes the text to speech engine
     * 
     * @param tts tts engine to close
     * @return always null
     */
    public static TextToSpeech shutdownTTS(final TextToSpeech tts) {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        } 

        return null;
    }

    /**
     * fetches the current version of the app
     * 
     * @param strSpacer spacer to use before version
     * @param ctx context
     * @return version string
     */
    public static String getVersionSuffix(final String strSpacer, final Context ctx) {
        try {
            final PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return strSpacer + pInfo.versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            /** ignore */
        }

        return "";
    }

    /**
     * uses the text to speech engine to speak out a word
     * 
     * @param strSpeakMe word to speak
     * @param tts tts engine to use
     * @param ctx context for the animation loading
     * @param v for teh snackbar
     * @param fab button to animate
     */
    public static void doSpeak(final String strSpeakMe, final TextToSpeech tts, final Context ctx, final View v,
                               final FloatingActionButton fab) {
        if (tts != null) {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString());
            tts.speak(strSpeakMe, TextToSpeech.QUEUE_FLUSH, params);
            fab.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.fab_rotate));
        } else {
            showSnackbar("Sprachausgabe deaktiviert", v, Snackbar.LENGTH_SHORT);
        }
    }

    /**
     * parses the readme from the resources to display in the drawer's "Über Lostwords"
     * 
     * @param act to access the ressources
     * @return parsed readme
     */
    public static String parseReadme(MainActivity act) {
        StringBuilder buff = new StringBuilder(1024);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(act.getResources().openRawResource(R.raw.readme)));
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                buff.append(strLine.replaceAll("#", "")).append("\n");
            }
        } catch(final Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return buff.toString();
    }
}