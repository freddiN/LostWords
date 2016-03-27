package de.freddi.android.lostwords;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.util.ArraySet;
import android.util.Log;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by freddi on 27.03.2016.
 */
public class FavoriteHandler {

    private FloatingActionButton m_fabFlav;
    private SharedPreferences m_settings;
    private String m_strSettingsFav;

    public FavoriteHandler(FloatingActionButton fabFlav, SharedPreferences settings, final String strSettingsFav) {
        this.m_fabFlav = fabFlav;
        this.m_settings = settings;
        this.m_strSettingsFav = strSettingsFav;
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

        checkFavorite(lw);
        return strReturn;
    }

    public Set<String> getFavorites() {
        return m_settings.getStringSet(m_strSettingsFav, new ArraySet<String>());
    }

    private void addToFavorites(final LostWord lw) {
        //Log.d("FAV", "addToFavorites " + lw.getWord());

        Set<String> setFavs = m_settings.getStringSet(m_strSettingsFav, new ArraySet<String>());
        setFavs.add(lw.getWord());

        pesistSet(setFavs);
    }

    private void removeFromFavorites(final LostWord lw) {
        //Log.d("FAV", "removeFromFavorites " + lw.getWord());

        Set<String> setFavs = m_settings.getStringSet(m_strSettingsFav, new ArraySet<String>());
        Iterator<String> iterator = setFavs.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element.equals(lw.getWord())) {
                iterator.remove();
            }
        }
        pesistSet(setFavs);
    }

    private void pesistSet(final Set<String> setFavs) {
        SharedPreferences.Editor editor = m_settings.edit();
        editor.putStringSet(m_strSettingsFav, setFavs);
        editor.commit();
    }
}
