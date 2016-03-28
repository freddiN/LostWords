package de.freddi.android.lostwords;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.util.ArraySet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by freddi on 27.03.2016.
 */
public class FavoriteHandler {

    private FloatingActionButton m_fabFlav;
    private Set<String> m_setFavs = new HashSet<String>();

    public FavoriteHandler(FloatingActionButton fabFlav, final Set<String> setFavs) {
        this.m_fabFlav = fabFlav;
        this.m_setFavs.addAll(setFavs);
    }

    public boolean checkFavorite(final LostWord lw) {
        if (m_setFavs.contains(lw.getWord()) ) {
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


    private void addToFavorites(final LostWord lw) {
        //Log.d("FAV", "addToFavorites " + lw.getWord());
        m_setFavs.add(lw.getWord());
    }

    private void removeFromFavorites(final LostWord lw) {
        //Log.d("FAV", "removeFromFavorites " + lw.getWord());

        Iterator<String> iterFavs = m_setFavs.iterator();
        while (iterFavs.hasNext()) {
            String element = iterFavs.next();
            if (element.equals(lw.getWord())) {
                iterFavs.remove();
            }
        }
    }

    public Set<String> getFavorites() {
        return this.m_setFavs;
    }
}
