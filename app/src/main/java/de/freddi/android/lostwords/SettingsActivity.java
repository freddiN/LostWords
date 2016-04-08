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

            m_tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(final int status) {
                    if(status != TextToSpeech.ERROR && m_tts != null) {
                        final Locale[] arrLocals = Locale.getAvailableLocales();
                        List<String>listEntries = new ArrayList<>(arrLocals.length);
                        List<String>listEntryValues = new ArrayList<>(arrLocals.length);

                        for (Locale l: arrLocals) {
                            if (!TextUtils.isEmpty(l.getDisplayLanguage()) &&
                                    //!TextUtils.isEmpty(l.toLanguageTag()) &&
                                    TextUtils.isEmpty(l.getDisplayCountry()) &&
                                    m_tts.isLanguageAvailable(l) == TextToSpeech.LANG_AVAILABLE ) {
                                listEntryValues.add(l.toString());
                                listEntries.add(l.getDisplayLanguage() + "  (" + l.getLanguage().toUpperCase() + ")");
                            }
                        }

                        m_tts.stop();
                        m_tts.shutdown();
                        m_tts = null;

                        ListPreference lp = (ListPreference)getPreferenceScreen().getPreference(3);
                        lp.setEntries(listEntries.toArray(new String[listEntries.size()]));
                        lp.setEntryValues(listEntryValues.toArray(new String[listEntryValues.size()]));
                    } else {
                        showSnackbar("Fehler beim Einlesen der Sprachen für TextToSpeech");
                    }
                }
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /** Anzeige wenn Einstellungen geändert werden */
            String strValue = "";
            String strKeyname = "";
            if (key.equals(getResources().getString(R.string.settings_shake))) {
                strKeyname = getResources().getString(R.string.settings_shake_title);
                strValue = "" + sharedPreferences.getBoolean(getResources().getString(R.string.settings_shake), false);
            } else if (key.equals(getResources().getString(R.string.settings_shake_timeout))) {
                strKeyname = getResources().getString(R.string.settings_shake_timeout_title);
                strValue = "" + sharedPreferences.getString(getResources().getString(R.string.settings_shake_timeout), "");
            } else if (key.equals(getResources().getString(R.string.settings_shake_strength))) {
                strKeyname = getResources().getString(R.string.settings_shake_strength_title);
                strValue = "" + sharedPreferences.getString(getResources().getString(R.string.settings_shake_strength), "");
            } else if (key.equals(getResources().getString(R.string.settings_tts_locale))) {
                strKeyname = getResources().getString(R.string.settings_tts_locale_title);
                strValue = "" + sharedPreferences.getString(getResources().getString(R.string.settings_tts_locale), "");
            }

            if (!TextUtils.isEmpty(strKeyname) && !TextUtils.isEmpty(strValue)) {
                showSnackbar(String.format("Einstellung \"%s\" auf \"%s\" geändert", strKeyname, strValue));
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

        private void showSnackbar(final String strText) {
            if (getView() != null) {
                Snackbar.make(getView(),
                        strText,
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }
}
