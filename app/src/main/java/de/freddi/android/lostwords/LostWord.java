package de.freddi.android.lostwords;

/**
 * Created by freddi on 25.03.2016.
 */
public class LostWord {
    private String m_strWort;
    private String m_strErklaerung;

    public LostWord(final String strWort, final String strErklaerung) {
        this.m_strWort = strWort;
        this.m_strErklaerung = strErklaerung;
    }

    public String getWort() {
        return this.m_strWort;
    }

    public String getErklaerung() {
        return this.m_strErklaerung;
    }
}
