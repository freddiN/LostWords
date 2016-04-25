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
    private Context mContext = null;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return m_listFavorites.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), android.R.layout.simple_list_item_1);
        mView.setTextViewText(android.R.id.text1, m_listFavorites.get(position));
        mView.setTextColor(android.R.id.text1, Color.BLACK);


        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(FavoritesWidget.ACTION_CLICK);
        final Bundle bundle = new Bundle();
        bundle.putString("clicked", m_listFavorites.get(position));
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

        final Set<String> setSavedFavoriters = mContext.getSharedPreferences("MainActivity", 0).
                 getStringSet(mContext.getResources().getString(R.string.settings_fav), new HashSet<String>());
        if (setSavedFavoriters.size() > 0) {
            Set<String> setSavedFavoritersSorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            setSavedFavoritersSorted.addAll(setSavedFavoriters);

            for (String strFav : setSavedFavoritersSorted) {
                m_listFavorites.add(strFav);
            }
        } else {
            m_listFavorites.add(mContext.getString(R.string.widget_click_empty));
        }
    }

    @Override
    public void onDestroy() {
        m_listFavorites.clear();
    }
}
