package de.freddi.android.lostwords;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.util.ArraySet;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by freddi on 27.03.2016.
 */
public class FavoriteHandler {

    private FloatingActionButton m_fabFlav;
    private SharedPreferences m_settings;
    private String m_strSettingsNameFavorites;

    public FavoriteHandler(FloatingActionButton fabFlav, SharedPreferences settings, final String strSettingsFav) {
        this.m_fabFlav = fabFlav;
        this.m_settings = settings;
        this.m_strSettingsNameFavorites = strSettingsFav;
    }

    public boolean checkFavorite(final LostWord lw) {
        final Set<String> setFavs = getFavorites();
        if (setFavs.contains(lw.getWord()) ) {
            /** ist in der Liste --> Button hat Entfernen Funktionalität */
            m_fabFlav.setImageResource(android.R.drawable.btn_star_big_off);
            //Log.d("FAV", "vorhanden");
            return true;
        } else {
            /** ist nicht in der Liste --> Button hat Hinzufügen Funktionalität */
            m_fabFlav.setImageResource(android.R.drawable.btn_star_big_on);
            //Log.d("FAV", "nicht vorhanden");
            return false;
        }
    }

    public String handleFavoriteFloatbuttonClick(final LostWord lw) {
        String strReturn = "";
        final boolean bIsPresent = checkFavorite(lw);
        if (bIsPresent) {
            removeFromFavorites(lw);
            strReturn = "\"" + lw.getWord() + "\" aus Favoriten entfernt";
        } else {
            addToFavorites(lw);
            strReturn = "\"" + lw.getWord() + "\" zu Favoriten hinzugefügt";
        }

        /** nach der Änderung: Nochmal checken, dadurch wird der Floatbutton aktualisiert */
        checkFavorite(lw);
        return strReturn;
    }

    /** für die Favoritenliste */
    public Set<String> getFavorites() {
        return m_settings.getStringSet(m_strSettingsNameFavorites, new ArraySet<String>());
    }

    private void addToFavorites(final LostWord lw) {
        //Log.d("FAV", "addToFavorites " + lw.getWord());

        Set<String> setFavs = getSetFromSettings();
        setFavs.add(lw.getWord());

        pesistSetToSettings(setFavs);
    }

    private void removeFromFavorites(final LostWord lw) {
        //Log.d("FAV", "removeFromFavorites " + lw.getWord());

        Set<String> setFavs = getSetFromSettings();
        Iterator<String> iterFavs = setFavs.iterator();
        while (iterFavs.hasNext()) {
            String element = iterFavs.next();
            if (element.equals(lw.getWord())) {
                iterFavs.remove();
            }
        }
        pesistSetToSettings(setFavs);
    }

    private Set<String> getSetFromSettings() {
        return m_settings.getStringSet(m_strSettingsNameFavorites, new ArraySet<String>());
    }

    private void pesistSetToSettings(final Set<String> setFavs) {
        SharedPreferences.Editor editor = m_settings.edit();
        editor.putStringSet(m_strSettingsNameFavorites, setFavs);
        editor.commit();
    }
}
