package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.speech.tts.TextToSpeech;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener  {

    private TextToSpeech m_tts = null;
    private ProgressBar m_progressBar = null;

    private GestureDetectorCompat m_gestureDetector = null;

    private FavoriteHandler m_favHandler = null;
    private WordHandler m_wordHandler = null;

    private SearchView m_searchView = null;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private final AtomicLong m_lastSensorUpdate = new AtomicLong(0);
    final AtomicBoolean m_isSensorCheck = new AtomicBoolean(false);

    private SharedPreferences m_settings = null;

    private FloatingActionButton m_fab_speak = null, m_fab_own = null;

    private String m_strSettingsLocale = "";

    /** beim Beenden der Activity */
    @Override
    public void onDestroy() {
        m_tts = Helper.shutdownTTS(m_tts);

        super.onDestroy();
    }

    /** beim Überdecken der Activity */
    @Override
    public void onPause() {
        super.onPause();

        /** Favs speichern */
        m_favHandler.settingsPersistFavorites(getPreferences(0), getResources().getString(R.string.settings_fav), findViewById(R.id.content_frame));
        
        /** Ownwords speichern */
        m_wordHandler.settingsPersistOwnwords(getPreferences(0), getResources().getString(R.string.settings_ownwords), findViewById(R.id.content_frame));

        senSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        /** beim Rücksprung aus den Settings und beim Start */
        reconfigureTTS();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        m_settings = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Wörter Init */
        m_wordHandler = new WordHandler(
                getResources().getStringArray(R.array.words),
                getPreferences(0).getStringSet(getResources().getString(R.string.settings_ownwords), new HashSet<String>()), 
                getContentResolver());
        Helper.showSnackbar(getResources().getString(R.string.init_words, m_wordHandler.getWordCount()), findViewById(android.R.id.content), Snackbar.LENGTH_SHORT);

        /** Gestures Init */
        m_gestureDetector = new GestureDetectorCompat(this, new LostWordsGestureListener(this));

        /** TTS setup passiert nun in der onResume()*/

        /** FloatButton TTS */
        m_fab_speak = (FloatingActionButton) findViewById(R.id.fab_speak);
        m_fab_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Helper.doSpeak(m_wordHandler.getCurrentWord().getWord(), m_tts, getApplicationContext(), findViewById(android.R.id.content), m_fab_speak);
            }
        });

        /** FloatButton Favorites */
        final FloatingActionButton fab_fav = (FloatingActionButton) findViewById(R.id.fab_fav);
        if (fab_fav != null) {
            fab_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Helper.showSnackbar(m_favHandler.handleFavoriteFloatbuttonClick(m_wordHandler.getCurrentWord(), getResources()), findViewById(android.R.id.content), Snackbar.LENGTH_SHORT);
                }
            });
            fab_fav.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));
        }

        /** FloatButton Own Word */
        m_fab_own = (FloatingActionButton) findViewById(R.id.fab_own);
        if (m_fab_own != null) {
            m_fab_own.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final LostWord lw = m_wordHandler.getCurrentWord();
                    displayOwnwordDialog(lw.getWord(), lw.getMeaning(), false);
                }
            });
        }
        
        /** Nav-Layout Setup */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        /** Favoritenhandler Init */
        m_favHandler = new FavoriteHandler(fab_fav, getPreferences(0).getStringSet(getResources().getString(R.string.settings_fav), new HashSet<String>()));

        /** Beschleunigungssensor Setup */
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        /** Nach dem Start: Erstes zufälliges Wort anzeigen */
        newWordAndUpdateView(IndexType.RANDOM);
    }
    
    private void updateProgressBar() {
        /** Progressbar Setup */
        m_progressBar = (ProgressBar) findViewById(R.id.progess);
        if (m_progressBar != null) {
            m_progressBar.setProgress(m_wordHandler.getCurrentPosition()+1);
            m_progressBar.setMax(m_wordHandler.getWordCount());
        }
    }

    private void displayCurrentWord() {
        final LostWord lw = m_wordHandler.getCurrentWord();

        final TextView textAnzahl = (TextView) findViewById(R.id.textWordCounts);
        if (textAnzahl != null) {
            textAnzahl.setText(
                    getResources().getString(R.string.main_current_word_numbers,
                            (m_wordHandler.getCurrentPosition() + 1),
                            m_wordHandler.getWordCount()));
        }

        final TextView textWort = (TextView) findViewById(R.id.textWordContent);
        if (textWort != null) {
            textWort.setText(getResources().getString(R.string.main_current_word_text, lw.getWord(), lw.getMeaning()));
        }
    }

    @Override
    public void onBackPressed() {
        resetSearchView();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            final String strSelect = intent.getDataString();
            if (strSelect != null) {
                m_wordHandler.selectWordByString(strSelect);
                updateView();
                resetSearchView();
            }
        } else if ("android.intent.action.SEARCH".equals(strAction)) {
            /** Suche ohne Suggestions */
            resetSearchView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        /** SearchView konfigurieren */
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        m_searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        m_searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    private void resetSearchView() {
        if (m_searchView != null) {
            m_searchView.clearFocus();
            m_searchView.setIconified(true);
            m_searchView.setIconified(true);
        }
    }

    /**
     * pressed button <
     * 
     * @param v
     */
    public void buttonPrev(final View v) {
        newWordAndUpdateView(IndexType.PREV);
     }

    /**
     * pressd button >
     * 
     * @param v
     */
    public void buttonNext(final View v) {
        newWordAndUpdateView(IndexType.NEXT);
    }

    public void newWordAndUpdateView(final IndexType i) {
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
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle("Favoriten");

            final View convertView = getLayoutInflater().inflate(R.layout.fav_listview, null);
            if (convertView != null) {
                builder.setView(convertView);
            }

            final View lv = convertView.findViewById(R.id.lv);
            if (lv != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        stringArray);
                ((ListView)lv).setAdapter(adapter);
                final AlertDialog dialog = builder.show();

                ((ListView)lv).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int nID, long id) {
                        m_wordHandler.selectWordByString(stringArray[nID]);
                        updateView();
                        dialog.dismiss();
                    }
                });
            }
        } else if (id == R.id.nav_ueber) {
            /** Navigation: Über LostWords */
            LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = li.inflate(R.layout.ueber_view, null, false);
   
            TextView tview = (TextView)view.findViewById(R.id.ueber_view_text);
            if (tview != null) {
                tview.setText(Helper.parseReadme(this) + getString(R.string.ueber_qrcode_prefix));
                Linkify.addLinks(tview, Linkify.WEB_URLS);
            }
            
            final AlertDialog d = new AlertDialog
                    .Builder(this)
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(getResources().getString(R.string.ueber_title) + Helper.getVersionSuffix(" - ", getApplicationContext()))
                    .setView(view)
                    .create();
            d.show();
        } else if (id == R.id.nav_close) {
            /** Navigation: Beenden
             * "Nicht empfehlenswert", sagt Google. "Mir egal", sagt Freddi.
             */
            m_favHandler.settingsPersistFavorites(getPreferences(0), getResources().getString(R.string.settings_fav), findViewById(R.id.content_frame));

            /** Ownwords speichern */
            m_wordHandler.settingsPersistOwnwords(getPreferences(0), getResources().getString(R.string.settings_ownwords), findViewById(R.id.content_frame));

            senSensorManager.unregisterListener(this);
            finish();
            m_tts = Helper.shutdownTTS(m_tts);
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
        } else if (id == R.id.nav_ownwords) {
            /** Navigation: Hinzufügen */
            displayOwnwordDialog("", "", true);
        }

        /** Navigationsbereich schliessen */
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void displayOwnwordDialog(final String strWord, final String strMeaning, final boolean isNewWord) {
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = li.inflate(R.layout.ownwords_view, null, false);
        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setPositiveButton(getString(R.string.ownwords_button_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            final String strWord = ((TextView) view.findViewById(R.id.ownword_word)).getText().toString();
            final String strMeaning = ((TextView) view.findViewById(R.id.ownword_meaning)).getText().toString();
            final boolean isFavorite = ((CheckBox)view.findViewById(R.id.ownword_favorite)).isChecked();

            m_wordHandler.addWord(strWord, strMeaning);
            if (isFavorite) {
                m_favHandler.addToFavorites(strWord, getResources());
            }

            /** position could have changed, so update view but keep current word */
            m_wordHandler.selectWordByString(strWord);
            updateView();

            Helper.showSnackbar(getResources().getString(R.string.ownwords_add, strWord), findViewById(android.R.id.content), Snackbar.LENGTH_SHORT);    
            }
        });
        if (!isNewWord && m_wordHandler.getCurrentWord().isOwnWord()) {
            ((CheckBox)view.findViewById(R.id.ownword_favorite)).setVisibility(View.INVISIBLE);
            d.setNegativeButton(getString(R.string.ownwords_button_remove), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String strWord = ((TextView) view.findViewById(R.id.ownword_word)).getText().toString();
                    m_wordHandler.deleteWord(strWord);
                    
                    m_favHandler.removeFromFavorites(new LostWord(strWord, "" ,false), getResources());
                    updateView();

                    Helper.showSnackbar(getResources().getString(R.string.ownwords_remove, strWord), findViewById(android.R.id.content), Snackbar.LENGTH_SHORT);
                }
            });
        }
        d.setNeutralButton(getString(R.string.ownwords_button_cancel), null)
        .setTitle(getString(R.string.ownwords_title))
        .setView(view)
        .setIcon(android.R.drawable.ic_input_add);
        d.create();
        
        if (!isNewWord && !TextUtils.isEmpty(strWord)) {
            ((TextView) view.findViewById(R.id.ownword_word)).setText(strWord);
            ((TextView) view.findViewById(R.id.ownword_meaning)).setText(strMeaning);
        }
        
        d.show();
    }

    /** Wort anzeigen, Progressbar updaten, Fav.FLoatbutton ggfs. updaten */
    private void updateView() {
        displayCurrentWord();

        updateProgressBar();

        final LostWord lw = m_wordHandler.getCurrentWord();
        m_favHandler.checkFavorite(lw);

        if (lw.isOwnWord() && m_fab_own.getVisibility() == View.INVISIBLE) {
            m_fab_own.setVisibility(View.VISIBLE);
            m_fab_own.clearAnimation();
            Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
            a.setStartOffset(0);
            a.setDuration(100);
            m_fab_own.startAnimation(a);
        } else if (!lw.isOwnWord() && m_fab_own.getVisibility() == View.VISIBLE) {
            m_fab_own.setVisibility(View.INVISIBLE);
            m_fab_own.clearAnimation();
            Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
            a.setStartOffset(0);
            a.setDuration(100);
            m_fab_own.startAnimation(a);
        }
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
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER &&
                m_isSensorCheck.compareAndSet(false, true)) {
            try {
                if (m_settings.getBoolean(getResources().getString(R.string.settings_shake), false)) {
                    final int nShakeTimeout = Integer.parseInt(
                            m_settings.getString(
                                    getResources().getString(R.string.settings_shake_timeout),
                                    getResources().getString(R.string.settings_shake_timeout_default)));
                    final long actualTime = sensorEvent.timestamp;
                    if (actualTime - m_lastSensorUpdate.get() < TimeUnit.SECONDS.toNanos(nShakeTimeout)) {
                        m_isSensorCheck.set(false);
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
                        m_lastSensorUpdate.set(actualTime);

                        newWordAndUpdateView(IndexType.RANDOM);
                        resetSearchView();
                        Helper.doSpeak(m_wordHandler.getCurrentWord().getWord(), m_tts, getApplicationContext(), findViewById(android.R.id.content), m_fab_speak);
                    }
                }   //Settings-if
            } finally {
                m_isSensorCheck.set(false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //ignorieren
    }

    public boolean isDrawerOpen() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        return drawer != null && drawer.isDrawerVisible(GravityCompat.START);
    }

    /** TTS Setup abhängig von den Settings */
    private void reconfigureTTS() {
        final String strSettingLocale = m_settings.getString(
                getResources().getString(R.string.settings_tts_locale),
                getResources().getString(R.string.settings_tts_locale_default));

        if (strSettingLocale.equals(m_strSettingsLocale)) {
            return;
        }

        m_tts = Helper.shutdownTTS(m_tts);

        m_tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(final int status) {
            if(status != TextToSpeech.ERROR && m_tts != null) {
                final int nResult = m_tts.setLanguage(new Locale(strSettingLocale));
                if (nResult != TextToSpeech.SUCCESS) {
                    Helper.showSnackbar("TextToSpeech Einrichtung für \"" + strSettingLocale + "\"gescheitert, Errorcode=" + nResult, findViewById(android.R.id.content), Snackbar.LENGTH_SHORT);
                    m_tts = Helper.shutdownTTS(m_tts);
                } else {
                    m_strSettingsLocale = strSettingLocale;
                    m_fab_speak.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));
                    m_tts.setOnUtteranceProgressListener(new TTSAudioManagerListener(MainActivity.this, m_tts));
                }
            }
            }
        });
    }
}