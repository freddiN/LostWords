package de.freddi.android.lostwords.widget;

/**
 * Created by freddi on 24.04.2016.
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import de.freddi.android.lostwords.R;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<String> m_listFavorites = new ArrayList();
    private Context m_context = null;

    public WidgetDataProvider(final Context context, final Intent intent) {
        m_context = context;
    }

    @Override
    public int getCount() {
        return m_listFavorites.size();
    }

    @Override
    public long getItemId(final int nPosition) {
        return nPosition;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(final int nPosition) {
        RemoteViews mView = new RemoteViews(m_context.getPackageName(), android.R.layout.simple_list_item_1);
        mView.setTextViewText(android.R.id.text1, m_listFavorites.get(nPosition));
        mView.setTextColor(android.R.id.text1, Color.BLACK);
        
        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(FavoritesWidget.ACTION_CLICK);
        final Bundle bundle = new Bundle();
        bundle.putString(FavoritesWidget.ITEM_CLICK, m_listFavorites.get(nPosition));
        fillInIntent.putExtras(bundle);
        mView.setOnClickFillInIntent(android.R.id.text1, fillInIntent);
        
        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        m_listFavorites.clear();

        final Set<String> setSavedFavoriters = m_context.getSharedPreferences("MainActivity", 0).
                 getStringSet(m_context.getResources().getString(R.string.settings_fav), new HashSet<String>());
        if (setSavedFavoriters.size() > 0) {
            Set<String> setSavedFavoritersSorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            setSavedFavoritersSorted.addAll(setSavedFavoriters);

            for (String strFav: setSavedFavoritersSorted) {
                m_listFavorites.add(strFav);
            }
        } else {
            m_listFavorites.add(m_context.getString(R.string.widget_click_empty));
        }
    }

    @Override
    public void onDestroy() {
        m_listFavorites.clear();
    }
}
