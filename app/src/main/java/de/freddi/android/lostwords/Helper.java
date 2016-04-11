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

/**
 * Created by freddi on 09.04.2016.
 */
public class Helper {

    public static void doLog(final String strLogMe) {
        Log.d("LOSTWORDS", strLogMe);
    }

    public static boolean isTouchWithinButtons(final Point pTouch, final View... buttons) {
        int[] location = new int[2];
        boolean bXinButton, bYinButton;
        for (View button: buttons) {
            /** Button Position */
            button.getLocationInWindow(location);

            if (button instanceof SearchView) {
                /** Double-Taps auf SearchView ignorieren */
                bYinButton = pTouch.y >= location[1] && pTouch.y <= location[1] + button.getHeight();
                return bYinButton;
            } else {
                /** Touch innerhalb des Buttons? */
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
     * @param strText
     * @param v
     * @param nDuration z.B. Snackbar.LENGTH_SHORT
     */
    public static void showSnackbar(final String strText, final View v, final int nDuration) {
        if (v != null) {
            Snackbar.make(v, strText, nDuration).show();
        }
    }

    public static TextToSpeech shutdownTTS(TextToSpeech tts) {
        /** TTS schliessen */
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            return null;
        } else {
            return tts;
        }
    }

    public static String getVersionSuffix(final String strSpacer, final Context ctx) {
        String strSuffix = "";
        try {
            final PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            strSuffix += strSpacer + pInfo.versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            /** ignore */
        }

        return strSuffix;
    }

    public static void doSpeak(final String strSpeakMe, final TextToSpeech tts, final Context ctx, final View v,
                               final FloatingActionButton fab) {
        if (tts != null) {
            tts.speak(strSpeakMe, TextToSpeech.QUEUE_FLUSH, null);
            fab.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.fab_rotate));
        } else {
            showSnackbar("Sprachausgabe deaktiviert", v, Snackbar.LENGTH_SHORT);
        }
    }

    public static String parseReadme(MainActivity act) {
        StringBuffer buff = new StringBuffer(1024);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(act.getResources().openRawResource(R.raw.readme)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                buff.append(line).append("\n");
            }
        } catch(final Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return buff.toString();
    }
}