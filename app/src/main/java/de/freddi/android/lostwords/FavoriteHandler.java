package de.freddi.android.lostwords;

import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by freddi on 27.03.2016.
 */
public class FavoriteHandler {

    private FloatingActionButton m_fabFlav;
    private Set<String> m_setFavs = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    public FavoriteHandler(FloatingActionButton fabFlav, final Set<String> setFavs) {
        this.m_fabFlav = fabFlav;
        this.m_setFavs.addAll(setFavs);
    }

    public boolean checkFavorite(final LostWord lw) {
        if (m_setFavs.contains(lw.getWord()) ) {
            /** ist in der Liste --> Button hat Entfernen Funktionalität */
            m_fabFlav.setImageResource(android.R.drawable.btn_star_big_off);
            return true;
        } else {
            /** ist nicht in der Liste --> Button hat Hinzufügen Funktionalität */
            m_fabFlav.setImageResource(android.R.drawable.btn_star_big_on);
            return false;
        }
    }

    public String handleFavoriteFloatbuttonClick(final LostWord lw, Resources res) {
        String strReturn = "";
        final boolean bIsPresent = checkFavorite(lw);
        if (bIsPresent) {
            removeFromFavorites(lw);
            strReturn = res.getString(R.string.favorites_remove, lw.getWord());
        } else {
            addToFavorites(lw);
            strReturn = res.getString(R.string.favorites_add, lw.getWord());
        }

        /** nach der Änderung: Nochmal checken, dadurch wird der Floatbutton aktualisiert */
        checkFavorite(lw);
        return strReturn;
    }

    private void addToFavorites(final LostWord lw) {
        m_setFavs.add(lw.getWord());
    }

    private void removeFromFavorites(final LostWord lw) {

        Iterator<String> iterFavs = m_setFavs.iterator();
        while (iterFavs.hasNext()) {
            String element = iterFavs.next();
            if (element.equals(lw.getWord())) {
                iterFavs.remove();
            }
        }
    }

    /** Anzeige im Menü */
    public Set<String> getFavorites() {
        return this.m_setFavs;
    }
}
