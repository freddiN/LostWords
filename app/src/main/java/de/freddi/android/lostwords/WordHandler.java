package de.freddi.android.lostwords;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by freddi on 27.03.2016.
 */
public class WordHandler {

    private Random m_rnd = new Random(System.nanoTime());
    private int m_nCurrentPositionInWordlist = 0;
    private List<LostWord> m_listWords = new ArrayList<LostWord>();

    // zieht die Liste aus den String Ressourcen (strings.xml)
    public WordHandler(final String[] strArrWords) {
        String[] strArrWord;
        for (String strWord: strArrWords) {
            strArrWord = strWord.split("-");
            this.m_listWords.add(new LostWord(strArrWord[0].trim(), strArrWord[1].trim()));
        }
    }

    public void generateNewPosition(final IndexType i) {
        if (i == IndexType.RANDOM) {
            m_nCurrentPositionInWordlist = m_rnd.nextInt(m_listWords.size() - 1);
        } else if (i == IndexType.NEXT) {
            m_nCurrentPositionInWordlist++;
        } else if (i == IndexType.PREV) {
            m_nCurrentPositionInWordlist--;
        }

        /** Umlauf in beide Richtungen */
        if (m_nCurrentPositionInWordlist < 0) {
            m_nCurrentPositionInWordlist = m_listWords.size() - 1;
        } else if (m_nCurrentPositionInWordlist >= m_listWords.size()) {
            m_nCurrentPositionInWordlist = 0;
        }
    }

    public int getWordCount() {
        return this.m_listWords.size();
    }

    public int getCurrentWordIndex() {
        return this.m_nCurrentPositionInWordlist;
    }

    public LostWord getCurrentWord() {
        return this.m_listWords.get(this.m_nCurrentPositionInWordlist);
    }

    public void selectGivenWord(final String strWord) {
        for (int i=0; i<this.m_listWords.size(); i++) {
            if (m_listWords.get(i).getWord().equals(strWord)) {
                this.m_nCurrentPositionInWordlist = i;
            }
        }
    }
}
