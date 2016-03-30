package de.freddi.android.lostwords;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Created by freddi on 27.03.2016.
 */
public class WordHandler {

    final private Random m_rnd = new Random(System.nanoTime());
    private int m_nCurrentPositionInWordlist = 0;
    private ContentResolver m_resolver;
    //private List<LostWord> m_listWords = new ArrayList<LostWord>();

    /** zieht die Liste aus den String Ressourcen (strings.xml) */
    public WordHandler(final String[] strArrWords, ContentResolver resolver) {
        m_resolver = resolver;
        int nIdx;
        ContentValues values;
        for (String strWord: strArrWords) {
            nIdx = strWord.indexOf("-");
            if (nIdx != -1) {
                values = new ContentValues();
                values.put(SelectionType.WORD.name(), strWord.substring(0, nIdx).trim());
                values.put(SelectionType.MEANING.name(), strWord.substring(nIdx + 1).trim());

                m_resolver.insert(WordContentProvider.CONTENT_URI, values);

                //m_listWords.add(new LostWord(strWord.substring(0, nIdx).trim(), strWord.substring(nIdx + 1).trim()));
            }
        }
    }

    public void generateNewPosition(final IndexType i) {
        int nSize = getWordCount();
        if (i == IndexType.NEXT) {
            m_nCurrentPositionInWordlist++;
        } else if (i == IndexType.PREV) {
            m_nCurrentPositionInWordlist--;
        } else {
            /** Random */
            m_nCurrentPositionInWordlist = m_rnd.nextInt(nSize - 1);
        }

        /** Umlauf in beide Richtungen */
        if (m_nCurrentPositionInWordlist < 0) {
            m_nCurrentPositionInWordlist = nSize - 1;
        } else if (m_nCurrentPositionInWordlist >= nSize) {
            m_nCurrentPositionInWordlist = 0;
        }
    }

    /** Anzahl Wörter */
    public int getWordCount() {
        int nCount = 0;
        Cursor cursor = m_resolver.query(WordContentProvider.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            nCount = cursor.getCount();
            cursor.close();
        }

        return nCount;
    }

    /** derzeitiger Wörter Index */
    public int getCurrentWordIndex() {
        return m_nCurrentPositionInWordlist;
    }

    /** derzeitiges Wort */
    public LostWord getCurrentWord() {
        LostWord wordReturn = null;
        Cursor cursor = m_resolver.query(WordContentProvider.CONTENT_URI, null, null, null, null);

        int nIndex = 0;
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (nIndex == m_nCurrentPositionInWordlist) {
                    wordReturn = new LostWord(
                            cursor.getString(cursor.getColumnIndex(SelectionType.WORD.name())),
                            cursor.getString(cursor.getColumnIndex(SelectionType.MEANING.name())))
                    ;
                    break;
                }
                nIndex++;
                cursor.moveToNext();
            }
            cursor.close();
        }

        return wordReturn;
    }

    /** Favorites: derzeitiges Wort auf den Bildschirm holen */
    public void selectGivenWord(final String strWord) {
        Cursor cursor = m_resolver.query(WordContentProvider.CONTENT_URI, null, null, null, null);

        int nIndex = 0;
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex(SelectionType.WORD.name())).equals(strWord)) {
                    break;
                }
                nIndex++;
                cursor.moveToNext();
            }
            cursor.close();
        }

        m_nCurrentPositionInWordlist = nIndex;
    }

    public int searchAndSelectFirst(String term) {
        int nHit = 0;
//        final Pattern pattern = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
//        for (int i = 0; i < m_listWords.size(); i++) {
//            final LostWord cur = m_listWords.get(i);
//
//            if (pattern.matcher(cur.getWord()).find() || pattern.matcher(cur.getMeaning()).find()) {
//                if (nHit == 0) {
//                    /** the first hit is the one that we want. uh uh uh, honey */
//                    m_nCurrentPositionInWordlist = i;
//                }
//                nHit++;
//            }
//        }
        return nHit;
    }
}
