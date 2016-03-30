package de.freddi.android.lostwords;

/**
 * Created by freddi on 25.03.2016.
 */
public class LostWord {
    private int m_nID;
    private String m_strWord;
    private String m_strMeaning;

    public LostWord(final int nID, final String strWord, final String strMeaning) {
        m_nID = nID;
        this.m_strWord = strWord;
        this.m_strMeaning = strMeaning;
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
