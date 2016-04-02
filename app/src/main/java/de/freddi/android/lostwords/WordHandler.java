package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by freddi on 27.03.2016.
 */
public class WordHandler {

    final private Random m_rnd = new Random(System.nanoTime());
    private ContentResolver m_resolver;

    private int m_nCachedCount = 0;
    private LostWord m_CachedWord = null;

    /** zieht die Liste aus den String Ressourcen (strings.xml) */
    public WordHandler(final String[] strArrWords, ContentResolver resolver) {
        m_resolver = resolver;

        /**
         * sicherheithalber: Wörterliste IMMER alphabetisch sortieren, wer weiss
         * wie die aus den Ressourcen zurückkommen und dann im ContentProvider
         * landen
         */
        Set<String> setWords = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (String strWord: strArrWords) {
            setWords.add(strWord);
        }

        int nIdxDash, nID = 0;
        String strWord;
        ContentValues values;
        Iterator<String> iter = setWords.iterator();
        while (iter.hasNext()) {
            strWord = iter.next();
            nIdxDash = strWord.indexOf("-");

            if (nIdxDash != -1) {
                values = new ContentValues();
                values.put(BaseColumns._ID, String.valueOf(nID++));
                values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, strWord.substring(0, nIdxDash).trim());
                values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, strWord.substring(nIdxDash + 1).trim());

                m_resolver.insert(WordContentProvider.CONTENT_URI, values);
                m_nCachedCount++;
            }
        }
    }

    public void generateNewPosition(final IndexType i) {
        int nID = 0;
        if (m_CachedWord != null) {
            nID = m_CachedWord.getID();
        }
        if (i == IndexType.NEXT) {
            nID++;
        } else if (i == IndexType.PREV) {
            nID--;
        } else {
            /** Random */
            nID = m_rnd.nextInt(m_nCachedCount - 1);
        }

        /** Umlauf in beide Richtungen */
        if (nID < 0) {
            nID = m_nCachedCount - 1;
        } else if (nID >= m_nCachedCount) {
            nID = 0;
        }

        updateCurrentWord(nID);
    }

    /** Anzahl Wörter */
    public int getWordCount() {
        return m_nCachedCount;
    }

    /** derzeitiger Wörter Index */
    public int getCurrentWordIndex() {
        return m_CachedWord.getID();
    }

    public LostWord getCurrentWord() {
        return m_CachedWord;
    }

    /** derzeitiges Wort */
    private void updateCurrentWord(final int nID) {
        LostWord wordReturn = null;
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.ID.name(),
                getSelection(String.valueOf(nID)),
                null);

        if (cursor != null && cursor.moveToFirst()) {
            m_CachedWord = new LostWord(
                nID,
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2))
            );

            cursor.close();
        }
    }

    /** Favorites: derzeitiges Wort auf den Bildschirm holen */
    public void selectGivenWord(final String strWord) {
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.WORD.name(),
                getSelection(strWord),
                null);

        if (cursor != null && cursor.moveToFirst()) {
            m_CachedWord = new LostWord(
                    cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2))
            );

            cursor.close();
        }
    }

    private String[] getSelection(final String strTerm) {
        String[] strValues = new String[1];
        strValues[0] = strTerm;
        return strValues;
    }
}
