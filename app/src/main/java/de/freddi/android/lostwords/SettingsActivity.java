package de.freddi.android.lostwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;


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
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            /** übernimmt die Werte aus der preferences.xml */
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /** Anzeige wenn Einstellugnen geändert werden */
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
            }

            if (!strKeyname.equals("") && !strValue.equals("")) {
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
            Snackbar.make(getView(),
                    strText,
                    Snackbar.LENGTH_LONG)
                .show();
        }
    }
}
