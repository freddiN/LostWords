package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.speech.tts.TextToSpeech;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener  {

    public static final String LOG_PREFIX = "LOSTWORDS";

    private TextToSpeech m_tts = null;
    private ProgressBar m_progressBar = null;

    private GestureDetectorCompat m_gestureDetector = null;

    private FavoriteHandler m_favHandler = null;
    private WordHandler m_wordHandler = null;

    private SearchView searchView = null;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long m_lastSensorUpdate = 0;

    private SharedPreferences m_settings = null;

    /** beim Beenden der Activity */
    @Override
    public void onDestroy() {
        shutdown();

        super.onDestroy();
    }

    /** beim Überdecken der Activity */
    @Override
    public void onPause() {
        /** Favs speichern */
        settingsPersistFavorites(m_favHandler.getFavorites());

        super.onPause();

        senSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_settings = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Wörter Init */
        m_wordHandler = new WordHandler(getResources().getStringArray(R.array.words), getContentResolver());
        showSnackbar(getResources().getString(R.string.init_words, m_wordHandler.getWordCount()));

        /** Gestures Init */
        m_gestureDetector = new GestureDetectorCompat(this, new LostwordsGestureListener());

        /** TTS setup */
        m_tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR && m_tts != null) {
                    int nResult = m_tts.setLanguage(Locale.GERMAN);
                    if (nResult != TextToSpeech.SUCCESS) {
                        nResult = m_tts.setLanguage(new Locale("de_DE"));
                        if (nResult != TextToSpeech.SUCCESS) {
                            final Locale[] arrLocales = Locale.getAvailableLocales();
                            Log.e(LOG_PREFIX, "error initializing TTS, error=" + nResult + " , available locales: " + Arrays.toString(arrLocales));
                            showSnackbar("TextToSpeech Einrichtung gescheitert, Errorcode " + nResult);
                            m_tts.stop();
                            m_tts.shutdown();
                            m_tts = null;
                        }
                    }
                }
            }
        });

        /**  FloatButton TTS */
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSpeak(m_wordHandler.getCurrentWord().getWord());
                fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate));
            }
        });
        fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));

        /** FloatButton Favorites */
        final FloatingActionButton fab_fav = (FloatingActionButton) findViewById(R.id.fab_fav);
        fab_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackbar(m_favHandler.handleFavoriteFloatbuttonClick(m_wordHandler.getCurrentWord(), getResources()));
            }
        });
        fab_fav.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));

        /** Nav-Layout Setup */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /** Progressbar Setup */
        m_progressBar = (ProgressBar) findViewById(R.id.progess);
        m_progressBar.setMax(this.m_wordHandler.getWordCount() - 1);

        /** Vavoritenhandler Init */
        m_favHandler = new FavoriteHandler(fab_fav, settingsReadFavorites());

        /** Beschleunigungssensor Setup */
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        /** Nach dem Start: Erstes zufälliges Wort anzeigen */
        newWordAndUpdateView(IndexType.RANDOM);
    }

    private void displayCurrentWord() {
        TextView textAnzahl = (TextView) findViewById(R.id.textWordCounts);
        textAnzahl.setText((m_wordHandler.getCurrentWordIndex() + 1) + " / " + m_wordHandler.getWordCount());

        final LostWord lw = m_wordHandler.getCurrentWord();

        TextView textWort = (TextView) findViewById(R.id.textWordContent);
        textWort.setText(lw.getWord() + "\n\n - - - \n\n" + lw.getMeaning());
    }

    @Override
    public void onBackPressed() {
        resetSearchView();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        final String strAction = intent.getAction();
        if ("android.Intent.action.SEARCHED".equals(strAction)) {
            String strSelect = intent.getDataString();
            if (strSelect != null) {
                m_wordHandler.selectGivenWord(strSelect);
                updateView();
                resetSearchView();
               // Log.d("SEARCHED", "SEARCHED id=" + strSelect);
            }
        } else if ("android.intent.action.SEARCH".equals(strAction)) {
            //Suche ohne Suggestions
            //showSnackbar("Bei der Suche muss eine Suggestion ausgewählt weren");
            resetSearchView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu);

        /** SearchView konfigurieren */
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    private void resetSearchView() {
        if (searchView != null) {
            searchView.clearFocus();
            searchView.setIconified(true);
            searchView.setIconified(true);
        }
    }

    /** Button < gedrückt */
    public void buttonPrev(View v) {
        newWordAndUpdateView(IndexType.PREV);
     }

    /** Button > gedrückt */
    public void buttonNext(View v) {
        newWordAndUpdateView(IndexType.NEXT);
    }

    private void newWordAndUpdateView(final IndexType i) {
        m_wordHandler.generateNewPosition(i);   //Next, Prev, Random
        updateView();
        resetSearchView();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        resetSearchView();

        final int id = item.getItemId();
        if (id == R.id.nav_favorites) {
            /** Navigation: Favoriten */
            final Set<String> setFavs = m_favHandler.getFavorites();
            final String[] stringArray = setFavs.toArray(new String[setFavs.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setIcon(android.R.drawable.btn_star_big_on);
            builder.setPositiveButton("Ok", null);
            builder.setTitle("Favoriten");

            View convertView = (View) getLayoutInflater().inflate(R.layout.fav_listview, null);
            builder.setView(convertView);

            ListView lv = (ListView) convertView.findViewById(R.id.lv);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    stringArray);
            lv.setAdapter(adapter);
            final AlertDialog dialog = builder.show();

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int nID, long id) {
                    //Log.d("FAV", "SELECTED " + nID + " " + stringArray[nID]);
                    m_wordHandler.selectGivenWord(stringArray[nID]);
                    updateView();
                    dialog.dismiss();
                }
            });
        } else if (id == R.id.nav_ueber) {
            /** Navigation: Über LostWords */
            StringBuffer buff = new StringBuffer(512);
            final String[] strArrLines = getResources().getStringArray(R.array.ueber_content);
            for (String strLine: strArrLines) {
                buff.append(strLine).append("\n");
            }

            // Linkify the message
            final SpannableString s = new SpannableString(buff);
            Linkify.addLinks(s, Linkify.WEB_URLS);

            final AlertDialog d = new AlertDialog
                .Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(getResources().getString(R.string.ueber_title) + getVersionSuffix(" - "))
                .setMessage(s)
                .create();
            d.show();

            // Make the textview clickable. Must be called after show()
            ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } else if (id == R.id.nav_close) {
            /** Navigation: Beenden
             * "Nicht empfehlenswert", sagt Google. "Mir egal", sagt Freddi.
             */
            settingsPersistFavorites(m_favHandler.getFavorites());
            senSensorManager.unregisterListener(this);
            finish();
            shutdown();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (id == R.id.nav_share) {
            /** Navigation: Teilen */
            final LostWord lw = this.m_wordHandler.getCurrentWord();

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_body, lw.getWord()));
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_chooser)));
        } else if (id == R.id.nav_settings) {
            /** Navigation: Einstellungen */
            startActivity(new Intent(this, SettingsActivity.class));
        }

        /** Navigationsbereich schliessen */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getVersionSuffix(final String strSpacer) {
        String strSuffix = "";
        try {
            final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            strSuffix += strSpacer + pInfo.versionName;
        } catch (final PackageManager.NameNotFoundException e) {
        }

        return strSuffix;
    }

    /** Wort anzeigen, Progressbar updaten, Fav.FLoatbutton ggfs. updaten */
    private void updateView() {
        displayCurrentWord();

        if (m_progressBar != null) {
            m_progressBar.setProgress(this.m_wordHandler.getCurrentWordIndex());
        }

        m_favHandler.checkFavorite(m_wordHandler.getCurrentWord());
    }

    /** alle Touch Events erstmal durch den GestureDetector leiten */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (m_gestureDetector != null) {
            m_gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER &&
                m_settings.getBoolean(getResources().getString(R.string.settings_shake), false)) {

            final int nShakeTimeout = Integer.parseInt(
                    m_settings.getString(
                            getResources().getString(R.string.settings_shake_timeout),
                            getResources().getString(R.string.settings_shake_timeout_default)));
            final long actualTime = sensorEvent.timestamp;
            if (actualTime - m_lastSensorUpdate < TimeUnit.SECONDS.toNanos(nShakeTimeout)) {
                return;
            }

            final float x = sensorEvent.values[0];
            final float y = sensorEvent.values[1];
            final float z = sensorEvent.values[2];

            final float accelationSquareRoot = (x * x + y * y + z * z)
                    / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

            final int nShakeStrength = Integer.parseInt(
                    m_settings.getString(
                            getResources().getString(R.string.settings_shake_strength),
                            getResources().getString(R.string.settings_shake_strength_default)));
            if (accelationSquareRoot >= nShakeStrength * 2)  {
                m_lastSensorUpdate = actualTime;

                newWordAndUpdateView(IndexType.RANDOM);
                resetSearchView();
                doSpeak(m_wordHandler.getCurrentWord().getWord());
            }
        }
    }

    private void doSpeak(final String strSpeakMe) {
        if (m_tts != null) {
            m_tts.speak(
                    strSpeakMe,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    strSpeakMe);
        } else {
            showSnackbar("Sprachausgabe deaktiviert");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //ignorieren
    }

    /** Gesture-Detector Logik */
    class LostwordsGestureListener extends GestureDetector.SimpleOnGestureListener {
        /** immer true, sonst werden nachfolgende Gestures ignoriert */
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        /** Swipe-Links und -rechts erkennen, dann selbe Aktion wie mit den Buttons */
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            /** keine Gestures wenn der Drawer offen ist */
            if (isDrawerOpen()) {
                return true;
            }

            final int nGestureMinimumSpeed = getResources().getInteger(R.integer.gesture_min_speed);
            if (velocityX > nGestureMinimumSpeed) {
                buttonPrev(null);
            } else if (velocityX < -nGestureMinimumSpeed) {
                buttonNext(null);
            }

            return true;
        }

        /** Doule-Tap Erkennung, aber über den Buttons und bei offenem Drawer ignorieren */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final Point pTouch = new Point((int)e.getAxisValue(0), (int)e.getAxisValue(1));
             if (!isDrawerOpen() &&
                !isTouchWithinButtons(pTouch,
                    findViewById(R.id.fab),
                    findViewById(R.id.fab_fav),
                    findViewById(R.id.buttonPrev),
                    findViewById(R.id.buttonNext),
                    findViewById(R.id.search))
                    ) {
                newWordAndUpdateView(IndexType.RANDOM);
                resetSearchView();
            }

            return true;
        }
    }

    private boolean isDrawerOpen() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        return drawer.isDrawerVisible(GravityCompat.START);
    }

    private boolean isTouchWithinButtons(final Point pTouch, final View... buttons) {
        int[] location = new int[2];
        boolean bXinButton, bYinButton;
        for (View button: buttons) {
            /** Button Position */
            button.getLocationInWindow(location);

            if (button instanceof SearchView ) {
                /** Double-Taps auf SearchView ignorieren */
                bYinButton = pTouch.y >= location[1] && pTouch.y <= location[1] + button.getHeight();
                return bYinButton;
            } else {
                /** Touch innerhalb des Buttons? */
                bXinButton = pTouch.x >= location[0] && pTouch.x <= location[0] + button.getWidth();
                bYinButton = pTouch.y >= location[1] && pTouch.y <= location[1] + button.getHeight();
                if (bXinButton && bYinButton) {
                    return true;
                }
            }
        }

        return false;
    }

    private void showSnackbar(final String strText) {
        Snackbar.make(findViewById(android.R.id.content),
                    strText,
                    Snackbar.LENGTH_SHORT)
                .show();
    }

    private void shutdown() {
        /** TTS schliessen */
        if (m_tts != null) {
            m_tts.stop();
            m_tts.shutdown();
        }
    }

    private Set<String> settingsReadFavorites() {
        return getPreferences(0).getStringSet(getResources().getString(R.string.settings_fav), new HashSet<String>());
    }

    private void settingsPersistFavorites(final Set<String> setFavs) {
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putStringSet(getResources().getString(R.string.settings_fav), setFavs);

        if (!editor.commit()) {
            showSnackbar("error writing favorites");
        }
    }
}
