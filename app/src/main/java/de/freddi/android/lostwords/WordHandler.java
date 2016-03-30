package de.freddi.android.lostwords;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by freddi on 27.03.2016.
 */
public class WordHandler {

    final private Random m_rnd = new Random(System.nanoTime());
    private int m_nCurrentID = 0, m_nCount = 0;
    private ContentResolver m_resolver;

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
                values.put(SelectionType.ID.name(), String.valueOf(nID++));
                values.put(SelectionType.WORD.name(), strWord.substring(0, nIdxDash).trim());
                values.put(SelectionType.MEANING.name(), strWord.substring(nIdxDash + 1).trim());

                m_resolver.insert(WordContentProvider.CONTENT_URI, values);
                m_nCount++;
            }
        }
    }

    public void generateNewPosition(final IndexType i) {
        int nSize = getWordCount();
        if (i == IndexType.NEXT) {
            m_nCurrentID++;
        } else if (i == IndexType.PREV) {
            m_nCurrentID--;
        } else {
            /** Random */
            m_nCurrentID = m_rnd.nextInt(nSize - 1);
        }

        /** Umlauf in beide Richtungen */
        if (m_nCurrentID < 0) {
            m_nCurrentID = nSize - 1;
        } else if (m_nCurrentID >= nSize) {
            m_nCurrentID = 0;
        }
    }

    /** Anzahl Wörter */
    public int getWordCount() {
//        int nCount = 0;
//        Cursor cursor = m_resolver.query(WordContentProvider.CONTENT_URI, null, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            nCount = cursor.getCount();
//            cursor.close();
//        }

        return m_nCount;
    }

    /** derzeitiger Wörter Index */
    public int getCurrentWordIndex() {
        return m_nCurrentID;
    }

    /** derzeitiges Wort */
    public LostWord getCurrentWord() {
        LostWord wordReturn = null;
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.ID.name(),
                getSelection(String.valueOf(m_nCurrentID)),
                null);

        if (cursor != null && cursor.moveToFirst()) {
            wordReturn = new LostWord(
                    cursor.getInt(cursor.getColumnIndex(SelectionType.ID.name())),
                    cursor.getString(cursor.getColumnIndex(SelectionType.WORD.name())),
                    cursor.getString(cursor.getColumnIndex(SelectionType.MEANING.name()))
            );

            cursor.close();
        }

        return wordReturn;
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
            m_nCurrentID = cursor.getInt(cursor.getColumnIndex(SelectionType.ID.name()));
            cursor.close();
        }
    }

    public int searchAndSelectFirst(String term) {
        int nHit = 0;

        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.ANY.name(),
                getSelection(term),
                null);

        if (cursor != null && cursor.moveToFirst()) {
            nHit = cursor.getCount();
            m_nCurrentID = cursor.getInt(cursor.getColumnIndex(SelectionType.ID.name()));
            cursor.close();
        }

        return nHit;
    }

    private String[] getSelection(final String strTerm) {
        String[] strValues = new String[1];
        strValues[0] = strTerm;
        return strValues;
    }
}
