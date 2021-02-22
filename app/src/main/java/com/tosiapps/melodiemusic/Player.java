package com.tosiapps.melodiemusic;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.tosiapps.melodiemusic.YouTubeDownloader.PermissionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Player extends AppCompatActivity implements
        View.OnClickListener,
        TextView.OnEditorActionListener,
        CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener {

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    private com.google.android.gms.ads.InterstitialAd mInterstitialAd;

    ImageView background_img, repeat, add_to_playlist, next_track, prev_track, moon, sun;
    WebView video;
    RelativeLayout rl, rl_cover;
    boolean mBounded, night = false;
    FloatingViewService mServer;
    boolean playing = true, createPlaylist = false;
    ArrayAdapter<String> deviceAdapter;
    String TAG = "TAG";

    YouTubePlayerView youtubePlayerView;
    ImageButton play_button, back_arrow, like, download;
    String id, title, genres, thumb, playlist, api_key = "AIzaSyDwj8Ts53uLopiKlN1aKMz2S2UC0Vz6kuc";
    int loop = 0, position_in_playlist = 0, all_tracks;
    
    ArrayList<String> saved_songs = new ArrayList<>();
    ArrayList<String> playlists = new ArrayList<>();
    ArrayList<String> playlists_songs = new ArrayList<>();
    ArrayList<String> tracks = new ArrayList<String>();
    ArrayList<String> songs = new ArrayList<String>();

    String [] playlist_names_r, titles_r, thumbs_r, genres_r, durations_r, id_r;


    /**
     * NAME, SAVED_SONGS DATAS
     */

    private View mPlayButtonLayout;
    private TextView mPlayTimeTextView, mPlayTimeTextViewActual, title_txt, genres_txt, playlist_txt;
    TinyDB tinydb;
    private Handler mHandler;
    private SeekBar mSeekBar;
    private boolean no_ads = false;
    private InterstitialAd interstitialAd;

    private YouTubePlayer mPlayer;

    private String[] videoIds = {"6JYIGclVQdw", "LvetJ9U_tVY"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        setContentView(R.layout.activity_player);



        interstitialAd = new InterstitialAd(this, "331110057759065_339560340247370");
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                Random r = new Random();
                int i1 = r.nextInt(4 - 1) + 1;
                if (i1 == 1 && !no_ads){
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_LONG).show();
            askPermission();
        }else{
            Intent intent_service = new Intent(this, FloatingViewService.class);
            intent_service.putExtra("id", id);
            intent_service.putExtra("thumb", thumb);
            intent_service.putExtra("title", title);
            intent_service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            this.startService(intent_service);
        }
     /*   Intent intent_service = new Intent(this, FloatingViewService.class);
        intent_service.putExtra("id", id);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.startService(intent_service);
            Toast.makeText(this, "BELEGE", Toast.LENGTH_LONG).show();

        } else {
            if (Settings.canDrawOverlays(this)) {
                this.startService(intent_service);
                Toast.makeText(this, "ELEGE", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_LONG).show();
                askPermission();
            }
            Toast.makeText(this, "TETAA", Toast.LENGTH_LONG).show();

        }*/

       /* Intent intent_service = new Intent(getApplicationContext(), StandOutExampleActivity.class);
        intent_service.putExtra("id", id);
        startActivity(intent_service);*/

        tinydb = new TinyDB(this);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {


            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }

        playlist_txt = findViewById(R.id.playlist);
        next_track = findViewById(R.id.next_track);
        prev_track = findViewById(R.id.prev_track);
//        moon = findViewById(R.id.moon);
        sun = findViewById(R.id.sun);
        rl = findViewById(R.id.rl);
        rl_cover = findViewById(R.id.cover_layout);

        Intent intent = getIntent();
        thumb = intent.getStringExtra("thumb");
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        genres = intent.getStringExtra("genres");
        playlist = intent.getStringExtra("playlist");

        if (playlist != null){
            playlist_txt.setText("Playlist: " + playlist);

            //get songs in playlist and save them to Array (tracks)
            TinyDB tinyDB = new TinyDB(this);
            no_ads = tinyDB.getBoolean("no_ads");
            songs = tinyDB.getListString("playlists_songs");
            int songs_in_playlist = 0;

            for (int i = 0; i < songs.size(); i++){
                String[] help_array = songs.get(i).split(";");
                if (help_array[0].equals(playlist)){
                    songs_in_playlist++;
                }
            }

            playlist_names_r = new String[songs_in_playlist];
            titles_r = new String[songs_in_playlist];
            thumbs_r = new String[songs_in_playlist];
            genres_r = new String[songs_in_playlist];
            durations_r = new String[songs_in_playlist];
            id_r = new String[songs_in_playlist];

            int k = 0;

            for (int i = 0; i < songs.size(); i++){
                try {
                    String[] help_array = songs.get(i).split(";");
                    if (help_array[0].equals(playlist)){
                        id_r[k] = help_array[1];
                        titles_r[k] = help_array[2];
                        thumbs_r[k] = help_array[3];
                        genres_r[k] = help_array[4];
                        tracks.add(id_r[k] + ";" + titles_r[k] + ";" + thumbs_r[k] + ";" + genres_r[k]);
                        k++;
                    }else{
                        Log.d("ERROR", "ERROR");
                    }
                }catch (IndexOutOfBoundsException e){
                }
            }

            //get position of song (if exist in playlist) in playlist
            all_tracks = tracks.size();
            String[] splitted;
            for (k = 0; k < tracks.size(); k++){
                splitted = tracks.get(k).split(";");
                if (splitted[1].equals(title)){
                    position_in_playlist = k;
                }
            }

            //onclick prev or next button but firstly we have to check position
            prev_track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position_in_playlist > 0){
                        stopService(new Intent(Player.this, FloatingViewService.class));
                        Intent intent = new Intent(getApplicationContext(), Player.class);
                        intent.putExtra("thumb", thumbs_r[position_in_playlist - 1]);
                        intent.putExtra("id", id_r[position_in_playlist - 1]);
                        intent.putExtra("title", titles_r[position_in_playlist - 1]);
                        intent.putExtra("genres", genres_r[position_in_playlist - 1]);
                        if (playlist != null)
                            intent.putExtra("playlist", playlist);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });

            next_track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position_in_playlist < all_tracks - 1){
                        stopService(new Intent(Player.this, FloatingViewService.class));
                        Intent intent = new Intent(getApplicationContext(), Player.class);
                        intent.putExtra("thumb", thumbs_r[position_in_playlist + 1]);
                        intent.putExtra("id", id_r[position_in_playlist + 1]);
                        intent.putExtra("title", titles_r[position_in_playlist + 1]);
                        intent.putExtra("genres", genres_r[position_in_playlist + 1]);
                        if (playlist != null)
                            intent.putExtra("playlist", playlist);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }

        int brightness = 0;
        try {
            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            brightness = 0;
            e.printStackTrace();
        }



      /*  moon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_cover.setVisibility(View.VISIBLE);
            }
        });*/

        int finalBrightness = brightness;
        sun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_cover.setVisibility(View.INVISIBLE);
               //Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, finalBrightness);

            }
        });

        // TODO: 5. 1. 2019

        background_img = (ImageView)findViewById(R.id.background_img);
        title_txt = (TextView)findViewById(R.id.title);
        play_button = (ImageButton)findViewById(R.id.play_video);
        repeat = findViewById(R.id.repeat);
        add_to_playlist = findViewById(R.id.add_to_playlist);
        back_arrow = findViewById(R.id.back_arrow);
        like = findViewById(R.id.like);
        download = findViewById(R.id.download);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        URL yahoo = null;
        try {
            yahoo = new URL("https://www.wawier.com/melodie/download.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            if (yahoo != null) {
                in = new BufferedReader(
                        new InputStreamReader(
                                yahoo.openStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String inputLine;

        while (true) {
            try {
                if ((inputLine = in.readLine()) != null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (inputLine != null && !inputLine.contains("elegebelege")){
            download.setVisibility(View.GONE);
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDownloadVideo("https://youtube.com/watch?v=" + id);
            }
        });

        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacksAndMessages(null);
                finish();
            }
        });

        saved_songs = tinydb.getListString("saved_songs");
        playlists = tinydb.getListString("playlists");
        playlists_songs = tinydb.getListString("playlists_songs");

        deviceAdapter = new ArrayAdapter<>(Player.this, android.R.layout.simple_list_item_1);
        deviceAdapter.add(getString(R.string.create_new_playlist));
        if (playlists.size() > 0){
            for(int i = 0; i < playlists.size(); i++){
                deviceAdapter.add(playlists.get(i));
            }
        }

        add_to_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set invisible YouTube player because it is above dialog
                mServer.setVisible(false);

                final AlertDialog.Builder builderSingle = new AlertDialog.Builder(Player.this);
                builderSingle.setTitle(getString(R.string.playlists));
                builderSingle.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mServer.setVisible(true);
                    }
                });
                builderSingle.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (!createPlaylist)
                            mServer.setVisible(true);
                        createPlaylist = false;
                    }
                });

                builderSingle.setAdapter(deviceAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            createPlaylist = true;
                            createPlaylistDialog();
                            mServer.setVisible(false);
                        }else{
                            mServer.setVisible(true);
                            int start_index = 0;
                            for (int j = 0; j < playlists_songs.size(); j++){
                                if (playlists_songs.get(j).split(";")[0].equals(playlists.get(i - 1))){
                                    start_index = j; // + 1
                                    break;
                                }
                            }
                            String[] splitted = playlists_songs.get(start_index).split(";");

                            if (splitted[2].contains(title)){
                                Toast.makeText(Player.this, getString(R.string.track_already_exist_in_this_playlist), Toast.LENGTH_SHORT).show();
                            }else {
                                playlists_songs.add(playlists.get(i - 1) + ";" + id + ";" + title + ";" + thumb + ";" + genres);
                                tinydb.putListString("playlists_songs", playlists_songs);
                                Toast.makeText(Player.this, getString(R.string.track_successfully_added_to_playlist), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                builderSingle.show();
            }
        });


        final String song_object = id + ";" + title + ";" + thumb + ";" + genres;

        if (saved_songs.contains(song_object)){
            like.setBackgroundResource(R.drawable.heart_green);
        }else{
            like.setBackgroundResource(R.drawable.heart_white);
        }

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saved_songs.contains(song_object)){
                    like.setBackgroundResource(R.drawable.heart_white);
                    saved_songs.remove(song_object);
                }else{
                    like.setBackgroundResource(R.drawable.heart_green);
                    saved_songs.add(song_object);
                }
                tinydb.putListString("saved_songs", saved_songs);
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loop == 0){
                    loop = 1;
                    repeat.setImageResource(R.drawable.repeat_green);
                }else{
                    loop = 0;
                    repeat.setImageResource(R.drawable.repeat);
                }
            }
        });


        title_txt.setText(title);

        //Add play button to explicitly play video in YouTubePlayerView
        mPlayButtonLayout = findViewById(R.id.video_control);
        findViewById(R.id.play_video).setOnClickListener(this);

        mPlayTimeTextView = (TextView)findViewById(R.id.play_time);
        mPlayTimeTextViewActual = (TextView)findViewById(R.id.play_time_actual);
        mSeekBar = (SeekBar)findViewById(R.id.video_seekbar);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser){
                    //mPlayTimeTextViewActual.setText(String.format("%s", formatTime(i * 1000)));
                    mServer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playing) {
                    mServer.playVideo();
                    play_button.setImageResource(android.R.drawable.ic_media_pause);
                    playing = true;
                } else {
                    mServer.pauseVideo();
                    play_button.setImageResource(android.R.drawable.ic_media_play);
                    playing = false;
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                overridePendingTransition(R.anim.slide_out, R.anim.slide_in);
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_video:
               /* if (null != mPlayer && !mPlayer.isPlaying()){
                    mPlayer.play();
                    play_button.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mPlayer.pause();
                    play_button.setImageResource(android.R.drawable.ic_media_play);
                }
                break;*/
        }
    }

    private void initializeView() {
        //startService(new Intent(Player.this, FloatingViewService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

   /*
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player, boolean wasRestored) {
        this.mPlayer = player;
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable(){
            public void run(){
                mSeekBar.setMax(mPlayer.getDurationMillis() / 1000);
                mSeekBar.setProgress(mPlayer.getCurrentTimeMillis() / 1000);
                mHandler.postDelayed(this, 1000);
                mPlayTimeTextView.setText(String.format("%s", formatTime(mPlayer.getDurationMillis())));
                mPlayTimeTextViewActual.setText(String.format("%s", formatTime(mPlayer.getCurrentTimeMillis())));
                if (mPlayer.getDurationMillis() == mPlayer.getCurrentTimeMillis() && mPlayer.getDurationMillis() > 0){
                    if (loop == 1) {
                        mPlayer.loadVideo(id);
                    }else{
                        play_button.setImageResource(android.R.drawable.ic_media_play);
                    }
                }
            }
        }, 1);
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        player.loadVideo(id);
        mSeekBar.setMax(mPlayer.getDurationMillis());

    }
*/
    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "" : hours + ":")
                + String.format("%02d:%02d", minutes % 60, seconds % 60);
    }

    @Override
    public void onBackPressed(){
        mHandler.removeCallbacksAndMessages(null);
        finish();
    }

    public void createPlaylistDialog(){
        mServer.setVisible(false);
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(Player.this);
        builderSingle.setTitle(getString(R.string.create_new_playlist));


        final EditText input = new EditText(this);
        input.setWidth(10);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builderSingle.setView(input);

        builderSingle.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String txt = input.getText().toString();
                createPlaylist(txt);
            }
        });

        builderSingle.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mServer.setVisible(true);
            }
        });
        builderSingle.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builderSingle.show();
    }

    public void createPlaylist(String name){
        playlists = tinydb.getListString("playlists");
        playlists.add(name);
        if (playlists.equals(name)){
            Toast.makeText(this, getString(R.string.playlist_with_name) + " " + name + " " + getString(R.string.already_exist), Toast.LENGTH_SHORT).show();
        }else{
            tinydb.putListString("playlists", playlists);
            playlists_songs.add(name + ";" + id + ";" + title + ";" + thumb + ";" + genres);
            tinydb.putListString("playlists_songs", playlists_songs);
            if (name.matches(""))
                Toast.makeText(this, getString(R.string.please_fill_playlist_name), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Playlist " + name + " " + getString(R.string.successfully_created), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        return false;
    }


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
           // Toast.makeText(Player.this, "Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mServer = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            FloatingViewService.LocalBinder mLocalBinder = (FloatingViewService.LocalBinder)service;
            mServer = mLocalBinder.getServerInstance();
            mServer.center();
            mServer.showNotification(title, thumb, true);
            mServer.setFocus(false);
            mServer.loadVideo(id, title, playlist);
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable(){
                public void run(){
                    try{
                        // if on status bar click play / pause
                        if (mServer.setIconToPlay()){
                            play_button.setImageResource(android.R.drawable.ic_media_play);
                        }else{
                            play_button.setImageResource(android.R.drawable.ic_media_pause);

                        }

                        //if clicked next track
                        if (mServer.playNextTrack()){
                            next_track.performClick();
                        }

                        if (mServer.playPrevTrack()){
                            prev_track.performClick();
                        }

                        mSeekBar.setMax(mServer.getDuration());
                        mSeekBar.setProgress(mServer.getCurrentSecond());
                        mHandler.postDelayed(this, 1000);
                        mPlayTimeTextView.setText(String.format("%s", formatTime((int) mServer.getDuration() * 1000)));
                        mPlayTimeTextViewActual.setText(String.format("%s", formatTime((int) mServer.getCurrentSecond() * 1000)));
                        if ((int) mServer.getDuration() == (int) mServer.getCurrentSecond() && (int) mServer.getDuration() > 0) {
                            if (loop == 1) {
                                mServer.loop(loop);
                            } else {
                                mServer.loop(loop);
                                play_button.setImageResource(android.R.drawable.ic_media_play);
                            }
                        }
                    }catch (NullPointerException e){
                        Log.d("NULL POINTER", e.getMessage());
                    }
                }
            }, 1);
        }
    };

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, FloatingViewService.class);
        mIntent.putExtra("title", title);
        mIntent.putExtra("thumb", thumb);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        mServer.setFocus(true);
        mServer.setVisible(true);
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
        finish();
    }
    private void doDownloadVideo(String link)
    {
        if (PermissionHandler.isPermissionGranted((Activity) this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage", 1000)) {
            DownloadActivity downloadActivity = new DownloadActivity();
            downloadActivity.getYoutubeDownloadUrl(this, link);
        }
    }

}