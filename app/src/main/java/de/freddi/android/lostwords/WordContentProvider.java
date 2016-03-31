package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by freddi on 30.03.2016.
 */
public class WordContentProvider extends ContentProvider {
    public static final String PROVIDER_NAME = "de.freddi.android.lostwords.Wordprovider";
    public static final String URL = "content://" + PROVIDER_NAME + "/words";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    private static final String[] COLUMNS = {
            SelectionType.ID.name(),
            SelectionType.WORD.name(),
            SelectionType.MEANING.name()};

    private Map<Integer, LostWord> m_mapWords = new HashMap<Integer, LostWord>(100);

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
            return matchForSearchView(uri);
        }

        int nID = -1;
        if (selection == null || selectionArgs == null) {
            return matchEverything();
        }
        if (selection.equals(SelectionType.ID.name())) {
            return matchByID(Integer.parseInt(selectionArgs[0]));
        } else {
            return matchByWordOrMeaning(selection, selectionArgs[0]);
        }
    }

    private boolean isSearchView(final Uri uri) {
        String strPath = uri.getEncodedPath();
//        Log.d("CURSOR", "Q=" + query + " strPath=" + strPath + "  uri.getLastPathSegment()=" + uri.getLastPathSegment());
        return (strPath.startsWith("/suggestion/"));
    }

    private MatrixCursor matchForSearchView(final Uri uri) {
        String query = uri.getLastPathSegment().toLowerCase();
//        Log.d("CURSOR", "matchForSearchView=" + query );

        if (query.equals("search_suggest_query") ||
                query.length() < 3) {
//            Log.d("CURSOR", "matchForSearchView ABBRUCH: " + query );
            return null;
        }

        MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA
        });
        int nCounter = 0;

        Pattern matchPattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);

        for (LostWord lw: m_mapWords.values()) {
            if (isMatching(SelectionType.ANY.name(), lw, matchPattern)) {
                /** Aufbau siehe hier: http://developer.android.com/guide/topics/search/adding-custom-suggestions.html */
//                Log.d("CURSOR", "isMatching true für " + query + " id=" + lw.getID() + " Word=" + lw.getWord() + " Meaning=" + lw.getMeaning() + " listsize=" + m_mapWords.size());
                matrixCursor.addRow(new Object[]{lw.getID(), lw.getWord(), lw.getMeaning(), lw.getWord()});

                nCounter++;
            }
        }

//        Log.d("CURSOR", "matchForSearchView ANzahl= " + nCounter);
        if (nCounter > 0) {
            return matrixCursor;
        } else {
            return null;
        }
    }

    private MatrixCursor matchByID(final int nID) {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        if (m_mapWords.containsKey(nID)) {
            LostWord lw = m_mapWords.get(nID);
            MatrixCursor.RowBuilder builder = matrixCursor.newRow();
            builder.add(SelectionType.ID.name(), lw.getID());
            builder.add(SelectionType.WORD.name(), lw.getWord());
            builder.add(SelectionType.MEANING.name(), lw.getMeaning());
        }

        return matrixCursor;
    }

    private MatrixCursor matchEverything() {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        for (LostWord lw: m_mapWords.values()) {
            MatrixCursor.RowBuilder builder = matrixCursor.newRow();
            builder.add(SelectionType.ID.name(), lw.getID());
            builder.add(SelectionType.WORD.name(), lw.getWord());
            builder.add(SelectionType.MEANING.name(), lw.getMeaning());
        }

        return matrixCursor;
    }

    private MatrixCursor matchByWordOrMeaning(final String strSelection, final String strMatch) {
        Pattern matchPattern = Pattern.compile(Pattern.quote(strMatch), Pattern.CASE_INSENSITIVE);

        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        for (LostWord lw: m_mapWords.values()) {
            if (isMatching(strSelection, lw, matchPattern)) {
                MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                builder.add(SelectionType.ID.name(), lw.getID());
                builder.add(SelectionType.WORD.name(), lw.getWord());
                builder.add(SelectionType.MEANING.name(), lw.getMeaning());
            }
        }

        return matrixCursor;
    }

    private boolean isMatching(final String strSelection, LostWord lw, Pattern pattern) {
         if ((strSelection.equals(SelectionType.WORD.name()) || strSelection.equals(SelectionType.ANY.name())) &&
                pattern.matcher(lw.getWord()).find()) {
             //Log.d("CURSOR", "isMatching 1 strSelection=" + strSelection + " id=" + lw.getID());
            return true;
        }

        if ((strSelection.equals(SelectionType.MEANING.name()) || strSelection.equals(SelectionType.ANY.name())) &&
                pattern.matcher(lw.getMeaning()).find()) {
            //Log.d("CURSOR", "isMatching 1 strSelection=" + strSelection + " id=" + lw.getID());
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int nID = values.getAsInteger(SelectionType.ID.name());

        if (!m_mapWords.containsKey(nID)) {
            m_mapWords.put(nID, new LostWord(
                    values.getAsInteger(SelectionType.ID.name()),
                    values.getAsString(SelectionType.WORD.name()),
                    values.getAsString(SelectionType.MEANING.name())));
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
