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

    private List<LostWord> m_listWords = new ArrayList<LostWord>();

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        if (selectionArgs != null) {
//            Log.d("CURSOR", "selection=" + selection + "-selectionArgs[0]=" + selectionArgs[0]);
//        } else {
//            Log.d("CURSOR", "selection=" + selection + "-selectionArgs=" + selectionArgs);
//        }

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

    private MatrixCursor matchByID(final int nID) {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        for (LostWord lw: m_listWords) {
            if (lw.getID() == nID) {
                MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                builder.add(SelectionType.ID.name(), lw.getID());
                builder.add(SelectionType.WORD.name(), lw.getWord());
                builder.add(SelectionType.MEANING.name(), lw.getMeaning());
                break;
            }
        }

        return matrixCursor;
    }

    private MatrixCursor matchEverything() {
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        for (LostWord lw: m_listWords) {
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
        for (LostWord lw: m_listWords) {
            if (isMatching(strSelection, lw, matchPattern)) {
                MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                builder.add(SelectionType.ID.name(), lw.getID());
                builder.add(SelectionType.WORD.name(), lw.getWord());
                builder.add(SelectionType.MEANING.name(), lw.getMeaning());
            }
        }

        //matrixCursor.setNotificationUri(getContext().getContentResolver(),uri);   //TODO: nötig?
        return matrixCursor;
    }


    private boolean isMatching(final String strSelection, LostWord lw, Pattern pattern) {
         if ((strSelection.equals(SelectionType.WORD.name()) || strSelection.equals(SelectionType.ANY.name())) &&
                pattern.matcher(lw.getWord()).find()) {
            return true;
        }

        if ((strSelection.equals(SelectionType.MEANING.name()) || strSelection.equals(SelectionType.ANY.name())) &&
                pattern.matcher(lw.getMeaning()).find()) {
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
        m_listWords.add(new LostWord(
                values.getAsInteger(SelectionType.ID.name()),
                values.getAsString(SelectionType.WORD.name()),
                values.getAsString(SelectionType.MEANING.name())));
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
