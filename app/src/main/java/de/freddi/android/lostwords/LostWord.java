package de.freddi.android.lostwords;

/**
 * Created by freddi on 25.03.2016.
 */
public class LostWord {
    private final int m_nID;
    private final String m_strWord;
    private final String m_strMeaning;

    public LostWord(final int nID, final String strWord, final String strMeaning) {
        m_nID = nID;
        m_strWord = strWord;
        m_strMeaning = strMeaning;
    }

    public int getID() {
        return this.m_nID;
    }

    public String getWord() {
        return this.m_strWord;
    }

    public String getMeaning() {
        return this.m_strMeaning;
    }
}
