package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
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

    private final Map<Integer, LostWord> m_mapWords = new HashMap<>(100);

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        if (selectionArgs != null) {
//            Log.d("CURSOR", "selection=" + selection + "-selectionArgs[0]=" + selectionArgs[0] + " uri=" + uri);
//        } else {
//            Log.d("CURSOR", "selection=" + selection + "-selectionArgs=" + selectionArgs + " uri=" + uri);
//        }

        if (isSearchView(uri)) {
            return matchByWordOrMeaning(uri);
        }

        if (selection.equals(SelectionType.ID.name())) {
            return matchByID(Integer.parseInt(selectionArgs[0]));
        } else if (selection.equals(SelectionType.WORD.name())) {
            return matchByWord(selectionArgs[0]);
        }

        return null;
    }

    private boolean isSearchView(final Uri uri) {
        String strPath = uri.getEncodedPath();
//        Log.d("CURSOR", "Q=" + query + " strPath=" + strPath + "  uri.getLastPathSegment()=" + uri.getLastPathSegment());
        return (strPath.startsWith("/suggestion/"));
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

        for (LostWord lw: m_mapWords.values()) {
            if (matchPattern.matcher(lw.getWord() + lw.getMeaning()).find()) {
                /** Aufbau siehe hier: http://developer.android.com/guide/topics/search/adding-custom-suggestions.html */
                matrixCursor.addRow(new Object[]{lw.getID(), lw.getWord(), lw.getMeaning(), lw.getWord()});
                nCounter++;
            }
        }

        if (nCounter > 0) {
            return matrixCursor;
        } else {
            return null;
        }
    }

    private MatrixCursor matchByID(final int nID) {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        if (m_mapWords.containsKey(nID)) {
            final LostWord lw = m_mapWords.get(nID);
            matrixCursor.addRow(new Object[]{lw.getID(), lw.getWord(), lw.getMeaning(), lw.getID()});
        }

        return matrixCursor;
    }

    private MatrixCursor matchByWord(final String strMatch) {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        for (LostWord lw: m_mapWords.values()) {
            if (lw.getWord().equalsIgnoreCase(strMatch)) {
                matrixCursor.addRow(new Object[]{lw.getID(), lw.getWord(), lw.getMeaning(), lw.getID()});
            }
        }

        return matrixCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int nID = values.getAsInteger(BaseColumns._ID);

        if (!m_mapWords.containsKey(nID)) {
            m_mapWords.put(nID, new LostWord(
                    nID,
                    values.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_1),
                    values.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_2)));
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
