package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.speech.tts.TextToSpeech;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextToSpeech m_tts = null;
    private ProgressBar m_progressBar = null;

    private GestureDetectorCompat m_gestureDetector = null;

    private FavoriteHandler m_favHandler = null;
    private WordHandler m_wordHandler = null;

    private SearchView searchView = null;

    /** beim Beenden der Activity */
    @Override
    public void onDestroy() {
        // TTS shutdown!
        if (m_tts != null) {
            m_tts.stop();
            m_tts.shutdown();
        }
        super.onDestroy();
    }

    /** beim Überdecken der Activity */
    @Override
    public void onPause() {
        /** Favs speichern */
        pesistFavoritesToSettings(m_favHandler.getFavorites());

        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Wörter Init */
        m_wordHandler = new WordHandler(getResources().getStringArray(R.array.words));
        showSnackbar(getResources().getString(R.string.init_words, m_wordHandler.getWordCount()));

        /** Gestures Init */
        m_gestureDetector = new GestureDetectorCompat(this, new LostwordsGestureListener());

        /** TTS setup */
        m_tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status != TextToSpeech.ERROR && m_tts != null) {
                m_tts.setLanguage(Locale.GERMAN);
            }
            }
        });

        /**  FloatButton TTS */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (m_tts != null) {
                m_tts.speak(
                        m_wordHandler.getCurrentWord().getWord(),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        m_wordHandler.getCurrentWord().getWord());
            }
            }
        });
        fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));

        /** FloatButton Favorites */
        FloatingActionButton fab_fav = (FloatingActionButton) findViewById(R.id.fab_fav);
        fab_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String strReturn = m_favHandler.handleFavoriteFloatbuttonClick(m_wordHandler.getCurrentWord());
                showSnackbar(strReturn);
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
        m_favHandler = new FavoriteHandler(fab_fav, readFavoritesFromSettings());

        /** Nach dem Start: Erstes zufälliges Wort anzeigen */
        defaultWordProgressFavHandling(IndexType.RANDOM);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!searchView.isIconified()) {
            resetSearchView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchViewOnQueryTextListener());

        return true;
    }

    private void resetSearchView() {
        searchView.clearFocus();
        searchView.setIconified(true);
    }

    /** Button < gedrückt */
    public void buttonPrev(View v) {
        defaultWordProgressFavHandling(IndexType.PREV);
    }

    /** Button > gedrückt */
    public void buttonNext(View v) {
        defaultWordProgressFavHandling(IndexType.NEXT);
    }

    private void defaultWordProgressFavHandling(final IndexType i) {
        m_wordHandler.generateNewPosition(i);   //Next, Prev, Random
        displayCurrentWord();
        showProgress();
        m_favHandler.checkFavorite(m_wordHandler.getCurrentWord());
    }

    private void showProgress() {
        if (m_progressBar != null) {
            m_progressBar.setProgress(this.m_wordHandler.getCurrentWordIndex());
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.nav_favorites) {
            /** Navigation: Favoriten */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Favoriten");
            builder.setIcon(android.R.drawable.btn_star_big_on);
            builder.setPositiveButton("Ok", null);

            final Set<String> setFavs = m_favHandler.getFavorites();
            final String[] stringArray = setFavs.toArray(new String[setFavs.size()]);

            builder.setItems(stringArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int nID) {
                    //Log.d("FAV", "SELECTED " + nID + " " + stringArray[nID]);
                    m_wordHandler.selectGivenWord(stringArray[nID]);
                    updateView();
                }
            });

            builder.setView(new ListView(this));
            builder.create().show();
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
                .setTitle(getResources().getString(R.string.ueber_title))
                .setMessage(s)
                .create();
            d.show();

            // Make the textview clickable. Must be called after show()
            ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } else if (id == R.id.nav_close) {
            /** Navigation: Beenden
             * "Nicht empfehlenswert", sagt Google. "Mir egal", sagt Freddi.
             */
            pesistFavoritesToSettings(m_favHandler.getFavorites());
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (id == R.id.nav_share) {
            /** Navigation: Teilen */
            final LostWord lw = this.m_wordHandler.getCurrentWord();

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_body, lw.getWord()));
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_chooser)));
        }

        /** Navigationsbereich schliessen */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateView() {
        displayCurrentWord();
        showProgress();
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
            //Log.d("Gestures", "onFling:\n" + event1.toString() + "\n" + event2.toString() + "\n" + velocityX + "\n" + velocityY);

            final int nGestureMinimumSpeed = getResources().getInteger(R.integer.gesture_min_speed);
            if (velocityX > nGestureMinimumSpeed) {
                buttonNext(null);
            } else if (velocityX < -nGestureMinimumSpeed) {
                buttonPrev(null);
            }

            return true;
        }

        /** Doule-Tap Erkennung, aber über den Buttons ignorieren */
        @Override
        public boolean onDoubleTap(MotionEvent e) {

            final Point pTouch = new Point((int)e.getAxisValue(0), (int)e.getAxisValue(1));
            if (!isTouchWithinButtons(pTouch,
                    findViewById(R.id.fab),
                    findViewById(R.id.fab_fav),
                    findViewById(R.id.buttonPrev),
                    findViewById(R.id.buttonNext))) {
                m_wordHandler.generateNewPosition(IndexType.RANDOM);
                updateView();
            }

            return true;
        }
    }

    private boolean isTouchWithinButtons(final Point pTouch, final View... buttons) {

        int[] location = new int[2];
        boolean bXinButton, bYinButton;
        for (View button: buttons) {
            /** Button posi */
            button.getLocationInWindow(location);

            /** Touch innerhalb? */
            bXinButton = pTouch.x >= location[0] && pTouch.x <= location[0] + button.getWidth();
            bYinButton = pTouch.y >= location[1] && pTouch.y <= location[1] + button.getHeight();
            if (bXinButton && bYinButton) {
                return true;
            };
        }

        return false;
    }

    private void showSnackbar(final String strText) {
        Snackbar.make(findViewById(android.R.id.content),
                    strText,
                    Snackbar.LENGTH_SHORT)
                .show();
    }

    private Set<String> readFavoritesFromSettings() {
        SharedPreferences settings = getPreferences(0);
        return settings.getStringSet(getResources().getString(R.string.settings_fav), new HashSet<String>());
    }

    private void pesistFavoritesToSettings(final Set<String> setFavs) {
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putStringSet(getResources().getString(R.string.settings_fav), setFavs);
        if (!editor.commit()) {
            showSnackbar("error writing favorites");
        }
    }

    private final class SearchViewOnQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(final String s) {
            search(s);
            resetSearchView();
            return true;
        }

        @Override
        public boolean onQueryTextChange(final String s) {
            return search(s);
        }

        private boolean search(final String s) {
            if (s.length() < 3) {
                return false;
            }

            m_wordHandler.searchAndSelectFirst(s);
            updateView();
            return true;
        }
    }
}
