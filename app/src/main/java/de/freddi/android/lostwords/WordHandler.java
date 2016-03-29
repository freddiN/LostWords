package de.freddi.android.lostwords;

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
    private List<LostWord> m_listWords = new ArrayList<LostWord>();

    /** zieht die Liste aus den String Ressourcen (strings.xml) */
    public WordHandler(final String[] strArrWords) {
        int nIdx;
        for (String strWord: strArrWords) {
            nIdx = strWord.indexOf("-");
            if (nIdx != -1) {
                m_listWords.add(new LostWord(strWord.substring(0, nIdx).trim(), strWord.substring(nIdx + 1).trim()));
            }
        }
    }

    public void generateNewPosition(final IndexType i) {
        if (i == IndexType.NEXT) {
            m_nCurrentPositionInWordlist++;
        } else if (i == IndexType.PREV) {
            m_nCurrentPositionInWordlist--;
        } else {
            /** Random */
            m_nCurrentPositionInWordlist = m_rnd.nextInt(m_listWords.size() - 1);
        }

        /** Umlauf in beide Richtungen */
        if (m_nCurrentPositionInWordlist < 0) {
            m_nCurrentPositionInWordlist = m_listWords.size() - 1;
        } else if (m_nCurrentPositionInWordlist >= m_listWords.size()) {
            m_nCurrentPositionInWordlist = 0;
        }
    }
    /** Anzahl Wörter */
    public int getWordCount() {
        return m_listWords.size();
    }

    /** derzeitiger Wörter Index */
    public int getCurrentWordIndex() {
        return m_nCurrentPositionInWordlist;
    }

    /** derzeitiges Wort */
    public LostWord getCurrentWord() {
        return m_listWords.get(m_nCurrentPositionInWordlist);
    }

    /** Favorites: derzeitiges Wort auf den Bildschirm holen */
    public void selectGivenWord(final String strWord) {
        for (int i=0; i<this.m_listWords.size(); i++) {
            if (m_listWords.get(i).getWord().equals(strWord)) {
                m_nCurrentPositionInWordlist = i;
            }
        }
    }

    public boolean searchAndSelectFirst(String term) {
        final Pattern pattern = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < m_listWords.size(); i++) {
            final LostWord cur = m_listWords.get(i);

            if (pattern.matcher(cur.getWord()).find() || pattern.matcher(cur.getMeaning()).find()) {
                m_nCurrentPositionInWordlist = i;
                return true;
            }
        }
        return false;
    }
}
