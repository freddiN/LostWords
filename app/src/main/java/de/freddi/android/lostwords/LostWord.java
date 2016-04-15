package de.freddi.android.lostwords;

/**
 * Created by freddi on 25.03.2016.
 * 
 * represents a word: the word, its meanign and an ID
 */
public class LostWord {
    private final int m_nID;
    private final String m_strWord;
    private final String m_strMeaning;

    /**
     * 
     * @param nID the number when parsing the ressources
     * @param strWord word
     * @param strMeaning meaning
     */
    public LostWord(final int nID, final String strWord, final String strMeaning) {
        m_nID = nID;
        m_strWord = strWord;
        m_strMeaning = strMeaning;
    }

    /**
     * 
     * @return
     */
    public int getID() {
        return m_nID;
    }

    /**
     * 
     * @return
     */
    public String getWord() {
        return m_strWord;
    }

    /**
     * 
     * @return
     */
    public String getMeaning() {
        return m_strMeaning;
    }
}
