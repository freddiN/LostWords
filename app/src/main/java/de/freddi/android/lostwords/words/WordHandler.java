package de.freddi.android.lostwords.words;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.freddi.android.lostwords.Helper;

/**
 * Created by freddi on 27.03.2016.
 */
public class WordHandler {

    private final Random m_rnd = new Random(System.nanoTime());
    private final ContentResolver m_resolver;

    private int m_nCurrentPosition = 0;
    private LostWord m_CachedWord = null;

    /** zieht die Liste aus den String Ressourcen (strings.xml) */
    public WordHandler(final String[] strArrWordsBase, final Set<String> strSetWordsOwn, ContentResolver resolver) {
        m_resolver = resolver;

        addWordarrayToContent(strArrWordsBase, 0);

        String[] strArrWordsOwn = new String[strSetWordsOwn.size()];
        strSetWordsOwn.toArray(strArrWordsOwn);
        addWordarrayToContent(strArrWordsOwn, 1);
    }

    /**
     * 
     * @param strArrWords words to add
     * @param nWordMarker 0 = base word, 1 = own word
     */
    private void addWordarrayToContent(final String[] strArrWords, final int nWordMarker) {
        if (strArrWords == null || strArrWords.length < 1) {
            return;
        }
        
        int nIdxDash;
        ContentValues values;
        for (String strWord: strArrWords) {
            nIdxDash = strWord.indexOf(" - ");

            if (nIdxDash != -1) {
                values = new ContentValues();
                values.put(BaseColumns._ID, nWordMarker); //0 = base word, 1 = own word
                values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, strWord.substring(0, nIdxDash).trim());
                values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, strWord.substring(nIdxDash + 3).trim());

                m_resolver.insert(WordContentProvider.CONTENT_URI, values);
            }
        }
    }

    /**
     * move through the words via next, previous or random
     * 
     * @param i NEXT, PREV or RANDOM
     */
    public void generateNewPosition(final IndexType i) {
        final int nWordCount = getWordCounts()[0];
        
        if (i == IndexType.NEXT) {
            m_nCurrentPosition++;
        } else if (i == IndexType.PREV) {
            m_nCurrentPosition--;
        } else {
            /** RANDOM */
            m_nCurrentPosition = m_rnd.nextInt(nWordCount);
        }

        /** Umlauf in beide Richtungen */
        if (m_nCurrentPosition < 0) {
            m_nCurrentPosition = nWordCount - 1;
        } else if (m_nCurrentPosition >= nWordCount) {
            m_nCurrentPosition = 0;
        }

        selectWordByPosition(m_nCurrentPosition);
    }

    /**
     * wordcounts
     * 
     * @return gesamt-basewords-ownwords
     */
    public int[] getWordCounts() {
        String strCounts = m_resolver.getType(WordContentProvider.CONTENT_URI);
        String[] arrCounts = strCounts.split("-");
        int[] nReturn = {Integer.parseInt(arrCounts[0]), Integer.parseInt(arrCounts[1]), Integer.parseInt(arrCounts[2])}; 
        return nReturn;
    }

    /**
     * current word
     * 
     * @return current word
     */
    public LostWord getCurrentWord() {
        return m_CachedWord;
    }

    /**
     * makes the word identified by the positon in the TreeSet
     * use by the buttons/swipes, random (doubletap, shake)
     * 
     * @param nPosition position within all the words
     */
    private void selectWordByPosition(final int nPosition) {
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.POSITION.name(),
                new String[]{String.valueOf(nPosition)},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            m_CachedWord = new LostWord(
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)),
                1 == cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
            );

            cursor.close();
        }
    }

    /**
     * makes the word identified by "strWord" the current word.
     * used from the serach view and favorites
     * 
     * @param strWord word to select
     */
    public void selectWordByString(final String strWord) {
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.WORD.name(),
                new String[]{strWord},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            m_CachedWord = new LostWord(
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)),
                    1 == cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))   
            );

            m_nCurrentPosition = cursor.getInt(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
            cursor.close();
        }
    }

    public String addWord(final String strWord, final String strMeaning) {
        ContentValues values = new ContentValues();
        values.put(BaseColumns._ID, 1); //1 = ownword!
        values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, strWord.trim());
        values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, strMeaning.trim());

        final Uri result = m_resolver.insert(WordContentProvider.CONTENT_URI, values);

        selectWordByString(strWord);
        
        return result.getLastPathSegment();
    }

    public boolean deleteWord(final String strWord) {
        final int nResult = m_resolver.delete(WordContentProvider.CONTENT_URI, strWord, null);
        generateNewPosition(IndexType.PREV);
        
        return nResult == 1;
    }
    
    public int getCurrentPosition() {
        return m_nCurrentPosition;
    }

    /**
     * persiste favs to app settings
     *
     * @param prefs application data
     * @param strSettingsKey key
     * @param v for the snackbar
     */
    public void settingsPersistOwnwords(SharedPreferences prefs, final String strSettingsKey, final View v) {
        Set<String> setOwnwords = new HashSet<>();
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.OWNWORDS.name(),
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                setOwnwords.add(cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)) + 
                " - " +
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));
 
                cursor.moveToNext();
            }
            cursor.close();
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(strSettingsKey, setOwnwords);

        if (!editor.commit()) {
            Helper.showSnackbar("Problem beim Speichern der Ownwords", v, Snackbar.LENGTH_SHORT);
        }
    }
}
