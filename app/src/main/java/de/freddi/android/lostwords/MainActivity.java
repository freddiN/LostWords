package de.freddi.android.lostwords;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
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
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    @Override
    public void onDestroy() {
        // TTS shutdown!
        if (m_tts != null) {
            m_tts.stop();
            m_tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.m_wordHandler = new WordHandler(getResources().getStringArray(R.array.words));

        // TTS setup
        m_tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status != TextToSpeech.ERROR && m_tts != null) {
                m_tts.setLanguage(Locale.GERMAN);
            }
            }
        });

        // FloatButton TTS
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

        // FloatButton Favorites
        FloatingActionButton fab_fav = (FloatingActionButton) findViewById(R.id.fab_fav);
        fab_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String strReturn = m_favHandler.handleFavoriteFloatbuttonClick(m_wordHandler.getCurrentWord());
                Snackbar.make(findViewById(android.R.id.content),
                        strReturn,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
        fab_fav.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open));

        Snackbar.make(findViewById(android.R.id.content),
                    getResources().getString(R.string.init_words, this.m_wordHandler.getWordCount()),
                    Snackbar.LENGTH_SHORT)
                .show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        m_progressBar = (ProgressBar) findViewById(R.id.progess);
        m_progressBar.setMax(this.m_wordHandler.getWordCount() - 1);

        m_favHandler = new FavoriteHandler(fab_fav, getPreferences(0),
                getResources().getString(R.string.settings_fav));

        m_wordHandler.generateNewPosition(IndexType.RANDOM);
        displayCurrentWord();
        showProgress();
        m_favHandler.checkFavorite(m_wordHandler.getCurrentWord());

        this.m_gestureDetector = new GestureDetectorCompat(this, new LostwordsGestureListener());
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
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void buttonNext(View v) {
        this.m_wordHandler.generateNewPosition(IndexType.NEXT);
        displayCurrentWord();
        showProgress();
        m_favHandler.checkFavorite(m_wordHandler.getCurrentWord());
    }

    public void buttonPrev(View v) {
        this.m_wordHandler.generateNewPosition(IndexType.PREV);
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
                    displayCurrentWord();
                    showProgress();
                    m_favHandler.checkFavorite(m_wordHandler.getCurrentWord());
                }
            });

            builder.setView(new ListView(this));
            builder.create().show();
        } else if (id == R.id.nav_ueber) {
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
            /** nicht empfehlenswert. Mir aber egal, darf der User entscheiden */
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (id == R.id.nav_share) {
            final LostWord lw = this.m_wordHandler.getCurrentWord();

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_body, lw.getWord()));
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_chooser)));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (m_gestureDetector != null) {
            m_gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    class LostwordsGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            /** immer true, sonst werden nachfolgende Gestures ignoriert */
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            //Log.d("Gestures", "onFling:\n" + event1.toString() + "\n" + event2.toString() + "\n" + velocityX + "\n" + velocityY);

            final int nGestureMinimumSpeed = getResources().getInteger(R.integer.gesture_min_speed);
            if (velocityX > nGestureMinimumSpeed) {
                buttonNext(null);
                showProgress();
            } else if (velocityX < -nGestureMinimumSpeed) {
                buttonPrev(null);
                showProgress();
            }

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
                /** DoubleTaps aus den Buttons ignorieren */
            final Point pTouch = new Point((int)e.getAxisValue(0), (int)e.getAxisValue(1));
            if (!fromWithinButton(pTouch, findViewById(R.id.fab)) &&
                !fromWithinButton(pTouch, findViewById(R.id.fab_fav)) &&
                !fromWithinButton(pTouch, findViewById(R.id.buttonPrev)) &&
                !fromWithinButton(pTouch, findViewById(R.id.buttonNext))) {
                m_wordHandler.generateNewPosition(IndexType.RANDOM);
                displayCurrentWord();
                showProgress();
            }

            return true;
        }
    }

    private boolean fromWithinButton(Point pTouch, View button) {
        int[] location = new int[2];
        button.getLocationInWindow(location);

        final boolean bXinButton = pTouch.x >= location[0] && pTouch.x <= location[0] + button.getWidth();
        final boolean bYinButton = pTouch.y >= location[1] && pTouch.y <= location[1] + button.getHeight();
        return bXinButton && bYinButton;
    }
}
