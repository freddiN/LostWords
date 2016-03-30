package de.freddi.android.lostwords;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by freddi on 30.03.2016.
 */
public class WordContentProvider extends ContentProvider {
    public static final String PROVIDER_NAME = "de.freddi.android.lostwords.Wordprovider";
    public static final String URL = "content://" + PROVIDER_NAME + "/words";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    private static final String[] COLUMNS = {SelectionType.WORD.name(), SelectionType.MEANING.name()};

    private List<LostWord> m_listWords = new ArrayList<LostWord>();

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // TODO: Daten aus der Liste holen
        // TODO: damit MatrixCursor erstelllen udn zurpckleifern

//        if (selectionArgs != null) {
//            Log.d("CURSOR", "selection=" + selection + "-selectionArgs[0]=" + selectionArgs[0]);
//        } else {
//            Log.d("CURSOR", "selection=" + selection + "-selectionArgs=" + selectionArgs);
//        }

        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        for (LostWord lw: m_listWords) {
            if (selection == null || selectionArgs == null || isMatching(selection, lw, selectionArgs[0])) {
                MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                builder.add(SelectionType.WORD.name(), lw.getWord());
                builder.add(SelectionType.MEANING.name(), lw.getMeaning());
            }
        }

        //matrixCursor.setNotificationUri(getContext().getContentResolver(),uri);   //TODO: nötig?
        return matrixCursor;
    }

    private boolean isMatching(final String strSelection, LostWord lw, String strCompare) {
        if ((strSelection.equals(SelectionType.WORD.name()) || strSelection.equals(SelectionType.ANY.name())) &&
            lw.getWord().equalsIgnoreCase(strCompare)) {
            return true;
        }

        if ((strSelection.equals(SelectionType.MEANING.name()) || strSelection.equals(SelectionType.ANY.name())) &&
                lw.getMeaning().contains(strCompare)) { //TODO: ignore case, pattern matcher
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
        m_listWords.add(new LostWord(values.getAsString(SelectionType.WORD.name()), values.getAsString(SelectionType.MEANING.name())));
        //m_matrixCursor.addRow(new String[]{values.getAsString(WordContentProvider.NAME), values.getAsString(WordContentProvider.VALUE)});
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
