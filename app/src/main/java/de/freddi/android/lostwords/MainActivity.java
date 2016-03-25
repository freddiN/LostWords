package de.freddi.android.lostwords;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;
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

    private List<LostWord> m_listWoerter = new ArrayList();
    private Random m_rnd = new Random(System.nanoTime());
    private int m_nCurrentPosition = 0;
    private TextToSpeech m_tts = null;

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

        // FloatButton random
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateNewIndex(IndexType.RANDOM);
                showWord();
            }
        });

        // FloatButton TTS
        FloatingActionButton fabSpeak = (FloatingActionButton) findViewById(R.id.fabSpeak);
        fabSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_tts != null) {
                    LostWord lw = m_listWoerter.get(m_nCurrentPosition);
                    m_tts.speak(lw.getWort(), TextToSpeech.QUEUE_FLUSH, null, lw.getWort());
                }
            }
        });

        initWoerter();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        generateNewIndex(IndexType.RANDOM);
        showWord();
    }

    // zieht die Liste aus den String Ressourcen (strings.xml)
    private void initWoerter() {
        String[] strArrWord;
        String[] strArrWords = getResources().getStringArray(R.array.words);
        for (String strWord: strArrWords) {
            strArrWord = strWord.split("-");
            this.m_listWoerter.add(new LostWord(strArrWord[0].trim(), strArrWord[1].trim()));
        }

        Toast.makeText(getApplicationContext(), "Reloade ok, " + this.m_listWoerter.size() + " Wörter geladen", Toast.LENGTH_SHORT).show();
    }

    private void generateNewIndex(IndexType i) {
        if (i == IndexType.RANDOM) {
            m_nCurrentPosition = m_rnd.nextInt(m_listWoerter.size() - 1);
        } else if (i == IndexType.NEXT) {
            m_nCurrentPosition++;
        } else if (i == IndexType.PREV) {
            m_nCurrentPosition--;
        }

        if (m_nCurrentPosition < 0) {
            m_nCurrentPosition = m_listWoerter.size() -1;
        } else if (m_nCurrentPosition >= m_listWoerter.size()) {
            m_nCurrentPosition = 0;
        }
    }

    private void showWord() {
        TextView textAnzahl = (TextView) findViewById(R.id.textAnzahl);
        textAnzahl.setText((m_nCurrentPosition + 1) + " / " + m_listWoerter.size());

        LostWord lw = m_listWoerter.get(m_nCurrentPosition);

        TextView textWort = (TextView) findViewById(R.id.textWortcontent);
        textWort.setText(lw.getWort() + "\n\n - - - \n\n" + lw.getErklaerung());
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
        generateNewIndex(IndexType.NEXT);
        showWord();
    }

    public void buttonPrev(View v) {
        generateNewIndex(IndexType.PREV);
        showWord();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        final int id = item.getItemId();
        if (id == R.id.nav_ueber) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.ueber_title));

            StringBuffer buff = new StringBuffer(512);
            String[] strArrLines = getResources().getStringArray(R.array.ueber_content);
            for (String strLine: strArrLines) {
                buff.append(strLine).append("\n");
            }

            builder.setMessage(buff.toString());
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });

            builder.create().show();
        } else if (id == R.id.nav_close) {
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (id == R.id.nav_share) {
            LostWord lw = m_listWoerter.get(m_nCurrentPosition);

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Ich habe gerade folgenden Ausdruck genutzt: \n\"" + lw.getWort() + "\"\nSponsored by LostWords for Android";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Oldschool Begriff Alert!");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Teile via"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
