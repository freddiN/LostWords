package de.freddi.android.lostwords.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import de.freddi.android.lostwords.Helper;
import de.freddi.android.lostwords.MainActivity;
import de.freddi.android.lostwords.R;
import de.freddi.android.lostwords.services.SpeechService;

/**
 * Implementation of App Widget functionality.
 */
public class FavoritesWidget extends AppWidgetProvider {
    public static final String ACTION_CLICK = "ACTION_CLICK";
    public static final String ITEM_CLICK = "ITEM_CLICK";   //see WidgetDataprovider.getViewAt()
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId: appWidgetIds) {
            RemoteViews mView = initViews(context, widgetId);
            appWidgetManager.updateAppWidget(widgetId, mView);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews initViews(final Context context, final int nWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorites_widget);

        /** Adapter zur Listenverwaltung an die ListView anhängen */
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, nWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widgetCollectionList, intent);

        /** Klick auf den WidgetHeader startet die App */
        Intent intentMainlaunch = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMainClick = PendingIntent.getActivity(context, 0, intentMainlaunch, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetLayoutMain, pendingIntentMainClick);

        /** Klick auf ein ListItem: ACTION_CLICK an FavoritesWidget */
        Intent clickIntent = new Intent(context, FavoritesWidget.class);
        clickIntent.setAction(FavoritesWidget.ACTION_CLICK);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, nWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widgetCollectionList, pendingIntent);

        return views;
    }

    @Override
    public void onEnabled(final Context context) {
    }

    @Override
    public void onDisabled(final Context context) {
      }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String strAction = intent.getAction();
         if (ACTION_CLICK.equals(strAction)) {
            final String item = intent.getExtras().getString(ITEM_CLICK, "");
            
            if (!item.equals(context.getString(R.string.widget_click_empty))) {
                Helper.invokeSpeechService(SpeechService.EXTRA_ACTION_SPEAK, item, context );
            }
         } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(strAction)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, FavoritesWidget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetCollectionList);
        }

        super.onReceive(context, intent);
    }
}

