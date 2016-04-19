package de.freddi.android.lostwords;

/**
 * Created by freddi on 25.03.2016.
 * 
 * represents a word: the word, its meanign and an ID
 */
public class LostWord implements Comparable<LostWord> {
    private final String m_strWord;
    private String m_strMeaning;
    private final boolean m_bIsOwnWord;

    /**
     * 
     * @param strWord word
     * @param strMeaning meaning
     * @param bIsOwnWord true = ownword, false = base word             
     */
    public LostWord(final String strWord, final String strMeaning, final boolean bIsOwnWord) {
        m_strWord = strWord;
        m_strMeaning = strMeaning;
        m_bIsOwnWord = bIsOwnWord;
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

    public void updateMeaning(final String strNewMeaning) {
        m_strMeaning = strNewMeaning;
    }

    /**
     *
     * @return
     */
    public boolean isOwnWord() {
        return m_bIsOwnWord;
    }
    

    @Override
    public int compareTo(final LostWord another) {
        return m_strWord.compareToIgnoreCase(another.getWord());
    }

    @Override
    public boolean equals(Object o) {
        return ((LostWord)o).getWord().equalsIgnoreCase(m_strWord);
    }

    @Override
    public int hashCode() {
        return m_strWord.toLowerCase().hashCode();
    }
}