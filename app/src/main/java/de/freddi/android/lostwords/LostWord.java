package de.freddi.android.lostwords;

/**
 * Created by freddi on 25.03.2016.
 */
public class LostWord {
    private String m_strWord;
    private String m_strMeaning;

    public LostWord(final String strWord, final String strMeaning) {
        this.m_strWord = strWord;
        this.m_strMeaning = strMeaning;
    }

    public String getWord() {
        return this.m_strWord;
    }

    public String getMeaning() {
        return this.m_strMeaning;
    }
}
