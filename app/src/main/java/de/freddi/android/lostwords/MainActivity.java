package de.freddi.android.lostwords;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import android.speech.tts.TextToSpeech;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<LostWord> m_listWords = new ArrayList();
    private Random m_rnd = new Random(System.nanoTime());
    private int m_nCurrentPositionInWordlist = 0;
    private TextToSpeech m_tts = null;

    private GestureDetectorCompat m_gestureDetector = null;

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
                LostWord lw = m_listWords.get(m_nCurrentPositionInWordlist);
                m_tts.speak(lw.getWord(), TextToSpeech.QUEUE_FLUSH, null, lw.getWord());
            }
            }
        });

        initializeWordlist();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        generateNewPosition(IndexType.RANDOM);
        showWord();

        this.m_gestureDetector = new GestureDetectorCompat(this, new LostwordsGestureListener());
    }

    // zieht die Liste aus den String Ressourcen (strings.xml)
    private void initializeWordlist() {
        String[] strArrWord;
        String[] strArrWords = getResources().getStringArray(R.array.words);
        for (String strWord: strArrWords) {
            strArrWord = strWord.split("-");
            this.m_listWords.add(new LostWord(strArrWord[0].trim(), strArrWord[1].trim()));
        }

        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.init_words, this.m_listWords.size()),
                Toast.LENGTH_SHORT).show();
    }

    private void generateNewPosition(final IndexType i) {
        if (i == IndexType.RANDOM) {
            m_nCurrentPositionInWordlist = m_rnd.nextInt(m_listWords.size() - 1);
        } else if (i == IndexType.NEXT) {
            m_nCurrentPositionInWordlist++;
        } else if (i == IndexType.PREV) {
            m_nCurrentPositionInWordlist--;
        }

        /** Umlauf in beide Richtungen */
        if (m_nCurrentPositionInWordlist < 0) {
            m_nCurrentPositionInWordlist = m_listWords.size() -1;
        } else if (m_nCurrentPositionInWordlist >= m_listWords.size()) {
            m_nCurrentPositionInWordlist = 0;
        }
    }

    private void showWord() {
        TextView textAnzahl = (TextView) findViewById(R.id.textAnzahl);
        textAnzahl.setText((m_nCurrentPositionInWordlist + 1) + " / " + m_listWords.size());

        LostWord lw = m_listWords.get(m_nCurrentPositionInWordlist);

        TextView textWort = (TextView) findViewById(R.id.textWortcontent);
        textWort.setText(lw.getWord() + "\n\n - - - \n\n" + lw.getMeaning());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
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
        generateNewPosition(IndexType.NEXT);
        showWord();
    }

    public void buttonPrev(View v) {
        generateNewPosition(IndexType.PREV);
        showWord();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        final int id = item.getItemId();
        if (id == R.id.nav_ueber) {
            StringBuffer buff = new StringBuffer(512);
            String[] strArrLines = getResources().getStringArray(R.array.ueber_content);
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
            LostWord lw = m_listWords.get(m_nCurrentPositionInWordlist);

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
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            //Log.d("Gestures", "onFling:\n" + event1.toString() + "\n" + event2.toString() + "\n" + velocityX + "\n" + velocityY);

            float flLimit = 3000;
            if (velocityX > flLimit) {
                buttonNext(null);
            } else if (velocityX < -flLimit) {
                buttonPrev(null);
            }

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            generateNewPosition(IndexType.RANDOM);
            showWord();

            return true;
        }
    }
}
