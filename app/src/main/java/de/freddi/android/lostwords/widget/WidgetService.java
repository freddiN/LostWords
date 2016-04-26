package de.freddi.android.lostwords.widget;

/**
 * Created by freddi on 24.04.2016.
 */
import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new WidgetDataProvider(getApplicationContext(), intent);
    }
}
