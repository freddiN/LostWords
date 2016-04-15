package de.freddi.android.lostwords;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by freddi on 27.03.2016.
 * 
 * Habdles everything about the drawer's favorities
 */
class FavoriteHandler {
    private final FloatingActionButton m_fabFav;
    private final Set<String> m_setFavs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * 
     * @param fabFav button to animate
     * @param setFavs favorite words
     */
    public FavoriteHandler(FloatingActionButton fabFav, final Set<String> setFavs) {
        m_fabFav = fabFav;
        m_setFavs.addAll(setFavs);
    }

    /**
     * if the given word is present the button has "remove from favs" functionality, and vice versa
     * 
     * @param lw word to check
     * @return true if found in favs, false otherwise
     */
    public boolean checkFavorite(final LostWord lw) {
        if (m_setFavs.contains(lw.getWord()) ) {
            /** ist in der Liste --> Button hat Entfernen Funktionalität */
            m_fabFav.setImageResource(android.R.drawable.btn_star_big_off);
            return true;
        } else {
            /** ist nicht in der Liste --> Button hat Hinzufügen Funktionalität */
            m_fabFav.setImageResource(android.R.drawable.btn_star_big_on);
            return false;
        }
    }

    /**
     * when the fav buttons is clicked
     * 
     * @param lw current word
     * @param res add/delete the selected word from here
     * @return info what has been done
     */
    public String handleFavoriteFloatbuttonClick(final LostWord lw, final Resources res) {
        String strReturn;
        final boolean bIsPresent = checkFavorite(lw);
        if (bIsPresent) {
            strReturn = removeFromFavorites(lw, res);
        } else {
            strReturn = addToFavorites(lw, res);
        }

        /** after the handling: check again to update the float button */
        checkFavorite(lw);
        return strReturn;
    }

    /**
     * adds to favs
     * 
     * @param lw word to add
     * @param res ressources
     * @return added info
     */
    private String addToFavorites(final LostWord lw, final Resources res) {
        m_setFavs.add(lw.getWord());
        return res.getString(R.string.favorites_add, lw.getWord());
    }

    /**
     * removes from favs
     * 
     * @param lw word to remove
     * @param res ressources
     * @return remove info
     */
    private String removeFromFavorites(final LostWord lw, final Resources res) {
        Iterator<String> iterFavs = m_setFavs.iterator();
        while (iterFavs.hasNext()) {
            if (iterFavs.next().equals(lw.getWord())) {
                iterFavs.remove();
            }
        }

        return res.getString(R.string.favorites_remove, lw.getWord());
    }


    /**
     * for the drawer display
     * 
     * @return favs
     */
    public Set<String> getFavorites() {
        return this.m_setFavs;
    }

    /**
     * persiste favs to app settings
     * 
     * @param prefs application data
     * @param strSettingsKey key
     * @param v for the snackbar
     */
    public void settingsPersistFavorites(SharedPreferences prefs, final String strSettingsKey, final View v) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(strSettingsKey, m_setFavs);

        if (!editor.commit()) {
            Helper.showSnackbar("Problem beim Speichern der Favoriten", v, Snackbar.LENGTH_SHORT);
        }
    }
}
