package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Created by freddi on 30.03.2016.
 */
public class WordContentProvider extends ContentProvider {
    private static final String PROVIDER_NAME = "de.freddi.android.lostwords.Wordprovider";
    private static final String URL = "content://" + PROVIDER_NAME + "/words";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    private static final String[] COLUMNS = {
        BaseColumns._ID,                            //ID
        SearchManager.SUGGEST_COLUMN_TEXT_1,        //Wort
        SearchManager.SUGGEST_COLUMN_TEXT_2,        //Meaning
        SearchManager.SUGGEST_COLUMN_INTENT_DATA    //nochmal ID
    };

    private final Set<LostWord> m_setWords = new TreeSet<>();

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        if (uri.getEncodedPath().startsWith("/suggestion/")) {
            /** aus der SearchView, konfiguriert in searchable.xml */
            return matchByWordOrMeaning(uri);
        }

        if (selection.equals(SelectionType.POSITION.name())) {
            return matchByPosition(Integer.parseInt(selectionArgs[0]));
        } else if (selection.equals(SelectionType.WORD.name())) {
            return matchByWord(selectionArgs[0]);
        } else if (selection.equals(SelectionType.OWNWORDS.name())) {
            return matchOwnWords();
        }

        return null;
    }

    private MatrixCursor matchByWordOrMeaning(final Uri uri) {
        final String query = uri.getLastPathSegment().toLowerCase();
        if (query.equals("search_suggest_query") ||
                query.length() < 3) {
            return null;
        }

        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        int nCounter = 0;

        final Pattern matchPattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
 
        int nPosition = 0;
        for (LostWord lw: m_setWords) {
           if (matchPattern.matcher(lw.getWord() + lw.getMeaning()).find()) {
                /** Aufbau siehe hier: http://developer.android.com/guide/topics/search/adding-custom-suggestions.html */
                matrixCursor.addRow(new Object[]{nPosition, lw.getWord(), lw.getMeaning(), lw.getWord()});
                nCounter++;
            }
            nPosition++;
        }

        if (nCounter > 0) {
            return matrixCursor;
        } else {
            return null;
        }
    }

    private MatrixCursor matchByPosition(final int nPosition) {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        if (nPosition >= 0 && nPosition <= m_setWords.size()-1) {
            LostWord lw = null;
            Iterator <LostWord> iter = m_setWords.iterator();
            for (int i=0; i<=nPosition; i++) {  //0 = erstes Element!
                lw = iter.next();
            }
            
            if (lw != null) {
                matrixCursor.addRow(new Object[]{lw.isOwnWord() ? 1 : 0, lw.getWord(), lw.getMeaning(), nPosition});
            }
        }

        return matrixCursor;
    }

    private MatrixCursor matchByWord(final String strMatch) {
        final LostWord lw = new LostWord(strMatch, "", false);
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        if (m_setWords.contains(lw)) {
            int nPosition = 0;
            for (LostWord lwIter: m_setWords) {
                if (lwIter.getWord().equalsIgnoreCase(strMatch)) {
                    matrixCursor.addRow(new Object[]{lwIter.isOwnWord() ? 1 : 0, lwIter.getWord(), lwIter.getMeaning(), String.valueOf(nPosition)});
                    return matrixCursor;
                }
                nPosition++;
            }
        }

        return matrixCursor;
    }

    private MatrixCursor matchOwnWords() {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);

        int nCounter = 0;
        for (LostWord lw: m_setWords) {
            if (lw.isOwnWord()) {
                /** Aufbau siehe hier: http://developer.android.com/guide/topics/search/adding-custom-suggestions.html */
                matrixCursor.addRow(new Object[]{0, lw.getWord(), lw.getMeaning(), ""});
                nCounter++;
            }
        }

        if (nCounter > 0) {
            return matrixCursor;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull final Uri uri) {
        int nBaseCount = 0, nOwnCount = 0;

        for (LostWord lw: m_setWords) {
           if (lw.isOwnWord()) {
               nOwnCount++;
           } else {
               nBaseCount++;
           }
        }
        
        return (nBaseCount+nOwnCount) + "-" + nBaseCount + "-" + nOwnCount;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        final LostWord lw = new LostWord(
                values.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_1),
                values.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_2),
                (1 == values.getAsInteger(BaseColumns._ID))
        );
        if (!m_setWords.contains(lw)) {
            m_setWords.add(lw);
        } else {
            for (LostWord lwTemp: m_setWords) {
                if (lwTemp.getWord().equalsIgnoreCase(lw.getWord())) {
                    lwTemp.updateMeaning(lw.getMeaning());
                 }
            }
        }

        return null;
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        final LostWord lw = new LostWord(selection, "", false);
        if (m_setWords.contains(lw)) {
            m_setWords.remove(lw);
            return 1;
        } else {
            return 0;    
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
