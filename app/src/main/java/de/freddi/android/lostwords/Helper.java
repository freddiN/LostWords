package de.freddi.android.lostwords;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.freddi.android.lostwords.services.SpeechService;

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
     * @param pTouch touch-position-coordinates
     * @param buttons list of buttons
     * @return true if touch from within one of the buttons, false otherwise
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

    /**
     * calsl the SpeechService
     * 
     * @param strAction see SpeechService statics
     * @param strParam see SpeechService statics
     * @param ctx
     */
    public static void invokeSpeechService(final String strAction, final String strParam, final Context ctx) {
        Intent speechIntent = new Intent(ctx, SpeechService.class);
        speechIntent.putExtra(SpeechService.EXTRA_ACTION, strAction);
        speechIntent.putExtra(SpeechService.EXTRA_PARAM, strParam);
        ctx.startService(speechIntent);
    }
}