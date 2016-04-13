package de.freddi.android.lostwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by freddi on 02.04.2016.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private TextToSpeech m_tts;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            /** übernimmt die Werte aus der preferences.xml */
            addPreferencesFromResource(R.xml.preferences);

            /** aktuelle Configwerte in die Configbeschreibung übernehmen */
            refreshConfigValues();

            /** Listenpunkt "Sprachausgabe" 
             * eigene TTS Instanz starten und wieder schliessen, um die Locales zu prüfen 
             */
            m_tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(final int status) {
                    if(status != TextToSpeech.ERROR && m_tts != null) {
                        final Locale[] arrLocals = Locale.getAvailableLocales();
                        List<String>listEntries = new ArrayList<>(arrLocals.length);        //echte Werte
                        List<String>listEntryValues = new ArrayList<>(arrLocals.length);    //nur optische Anzeige

                        for (Locale l: arrLocals) {
                            if (!TextUtils.isEmpty(l.getDisplayLanguage()) &&
                                    TextUtils.isEmpty(l.getDisplayCountry()) &&
                                    m_tts.isLanguageAvailable(l) == TextToSpeech.LANG_AVAILABLE ) {
                                listEntryValues.add(l.toString());
                                listEntries.add(l.getDisplayLanguage() + "  (" + l.getLanguage().toUpperCase() + ")");
                            }
                        }

                        m_tts = Helper.shutdownTTS(m_tts);

                        ListPreference lp = (ListPreference)getPreferenceScreen().getPreference(3);
                        lp.setEntries(listEntries.toArray(new String[listEntries.size()]));
                        lp.setEntryValues(listEntryValues.toArray(new String[listEntryValues.size()]));
                    } else {
                        Helper.showSnackbar("Fehler beim Einlesen der Sprachen für TextToSpeech", getView(), Snackbar.LENGTH_LONG);
                    }
                }
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, final String strKey) {
            /** Anzeige wenn Einstellungen geändert werden */
            String strValue = "";
            String strKeyname = "";
            if (strKey.equals(getResources().getString(R.string.settings_shake))) {
                strKeyname = getResources().getString(R.string.settings_shake_title);
                strValue = "" + sharedPreferences.getBoolean(getResources().getString(R.string.settings_shake), false);
            } else if (strKey.equals(getResources().getString(R.string.settings_shake_timeout))) {
                strKeyname = getResources().getString(R.string.settings_shake_timeout_title);
                strValue = "" + sharedPreferences.getString(getResources().getString(R.string.settings_shake_timeout), "");
            } else if (strKey.equals(getResources().getString(R.string.settings_shake_strength))) {
                strKeyname = getResources().getString(R.string.settings_shake_strength_title);
                strValue = "" + sharedPreferences.getString(getResources().getString(R.string.settings_shake_strength), "");
            } else if (strKey.equals(getResources().getString(R.string.settings_tts_locale))) {
                strKeyname = getResources().getString(R.string.settings_tts_locale_title);
                strValue = "" + sharedPreferences.getString(getResources().getString(R.string.settings_tts_locale), "");
            }

            if (!TextUtils.isEmpty(strKeyname) && !TextUtils.isEmpty(strValue)) {
                refreshConfigValues();
                Helper.showSnackbar(String.format("Einstellung \"%s\" auf \"%s\" geändert", strKeyname, strValue), getView(), Snackbar.LENGTH_LONG);
            }
        }
        
        /**
         *  aktuelle Config in den Listenpunkten anzeigen
         *  
         *  1 = Schütteltimeout
         *  2 = SchüttelStärke
         *  3 = Sprachausgabe
         *  summary enthält IMMER bereits "(aktuell: )", dieser Teil muss ersetzt werden
         */
        public void refreshConfigValues() {
            ListPreference lp;
            String strSummary;
            
            for (int i=1; i<=3; i++) {
                lp = (ListPreference)getPreferenceScreen().getPreference(i);
                strSummary = String.valueOf(lp.getSummary());
                lp.setSummary(strSummary.substring(0, strSummary.indexOf("(")) + "(aktuell: " + lp.getValue() + ")");
             }
        }

        @Override
        public void onResume() {
            super.onResume();

            /** Listener wenn ein Configwert geändert wird */
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();

            /** Listener wenn ein Configwert geändert wird */
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
