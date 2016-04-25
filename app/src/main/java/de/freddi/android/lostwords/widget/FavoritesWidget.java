package de.freddi.android.lostwords.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
    public static final String ACTION_UPDATE = "ACTION_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId: appWidgetIds) {
            Helper.doLog("FavoritesWidget onUpdate " + widgetId);
            RemoteViews mView = initViews(context, widgetId);
            appWidgetManager.updateAppWidget(widgetId, mView);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews initViews(Context context, int widgetId) {
        RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.favorites_widget);

        /** Adapter zur Listenverwaltung an die ListView anhängen */
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        mView.setRemoteAdapter(R.id.widgetCollectionList, intent);

        /** Klick auf den WidgetHeader startet die App */
        Intent intentMainlaunch = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMainClick = PendingIntent.getActivity(context, 0, intentMainlaunch, PendingIntent.FLAG_UPDATE_CURRENT);
        mView.setOnClickPendingIntent(R.id.widgetLayoutMain, pendingIntentMainClick);

        /** Klick auf ein ListItem: ACTION_CLICK an FavoritesWidget */
        Intent clickIntent = new Intent(context, FavoritesWidget.class);
        clickIntent.setAction(FavoritesWidget.ACTION_CLICK);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mView.setPendingIntentTemplate(R.id.widgetCollectionList, toastPendingIntent);

        return mView;
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
      }

    @Override
    public void onReceive(Context context, Intent intent) {
        Helper.doLog("FavoritesWidget onReceive action=" + intent.getAction());
        if (intent.getAction().equals(ACTION_CLICK)) {
            final String item = intent.getExtras().getString("clicked");
            
            Helper.doLog("FavoritesWidget onReceive2 " + item);
            if (!item.equals(context.getString(R.string.widget_click_empty))) {
                Helper.invokeSpeechService(SpeechService.EXTRA_ACTION_SPEAK, item,context );
            }
         } 
        
        super.onReceive(context, intent);
    }
}

