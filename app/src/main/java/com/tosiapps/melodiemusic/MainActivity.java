package com.tosiapps.melodiemusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.tosiapps.melodiemusic.YouTubeDownloader.PermissionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    HomeFrag homefrg = new HomeFrag();
    TopFrag topfrg = new TopFrag();
    TinyDB tinydb;
    boolean no_ads = false;
    PlaylistFrag playlistfrg = new PlaylistFrag();
    SettingsFrag settingsfrg = new SettingsFrag();
    DownloadFrag downloadfrg = new DownloadFrag();

    private FirebaseAnalytics mFirebaseAnalytics;
    private final String TAG = MainActivity.class.getSimpleName();
    private InterstitialAd interstitialAd;

    BottomNavigationView bottomNavigationView;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        FragmentManager manager = getSupportFragmentManager();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                        ft.replace(R.id.rl, homefrg, "detailFragment");
                        ft.commitAllowingStateLoss();
                        return true;
                    case R.id.navigation_top:
                        manager = getSupportFragmentManager();
                        Bundle bundle = new Bundle();
                        bundle.putString("country", "us");
                        topfrg.setArguments(bundle);
                        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                        ft1.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                        ft1.replace(R.id.rl, topfrg, "detailFragment");
                        ft1.commitAllowingStateLoss();
                        return true;
                    case R.id.navigation_playlist:
                        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                        ft2.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                        ft2.replace(R.id.rl, playlistfrg, "detailFragment");
                        ft2.commit();
                        return true;
                    case R.id.navigation_downloaded:
                        if (PermissionHandler.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage", 1000)) {
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                            ft4.replace(R.id.rl, downloadfrg, "detailFragment");
                            ft4.commit();
                        }
                        return true;
                    case R.id.navigation_settings:
                        FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                        ft3.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                        ft3.replace(R.id.rl, settingsfrg, "detailFragment");
                        ft3.commit();
                        return true;
                }
                return false;
            };

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );






    /*    ImageView fav = findViewById(R.id.fav);
        fav.setOnClickListener(view -> {
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.rl, new FavouriteFrag()).commit();
        });*/

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.rl, new HomeFrag(), "Home");
        transaction.addToBackStack(null);
        transaction.commit();

        tinydb = new TinyDB(this);
        no_ads = tinydb.getBoolean("no_ads");

        if (tinydb.getInt("first_time") != 5){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.welcome_to_melodie));
            alert.setMessage(getString(R.string.wtm_message));
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //Your action here
                }
            });

            alert.show();
            tinydb.putInt("first_time", 5);
        }


        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/proxima.otf");

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        setContentView(R.layout.activity_main);


        //check internet connection
        Config config = new Config();
    /*    if (!config.isNetworkConnected(this)){
            config.displayNetworkErrorBox(this);
        }*/

        RelativeLayout rl = findViewById(R.id.rl);
        rl.setVisibility(View.INVISIBLE);
        RelativeLayout splash = findViewById(R.id.splash);

        BottomNavigationViewEx navigation = findViewById(R.id.bnve);


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
            navigation.getMenu().removeItem(R.id.navigation_downloaded);
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Intent intent = getIntent();
        if (intent.getStringExtra("fragment") == null){
            new Handler().postDelayed(() -> {
                splash.setVisibility(View.INVISIBLE);
                rl.setVisibility(View.VISIBLE);
                getSupportActionBar().show();
                navigation.setVisibility(View.VISIBLE);
            }, 7000);
        }else{
            splash.setVisibility(View.INVISIBLE);
            rl.setVisibility(View.VISIBLE);
            getSupportActionBar().show();
            navigation.setVisibility(View.VISIBLE);

            switch (intent.getStringExtra("fragment")){
                case "home":
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                    ft.replace(R.id.rl, homefrg, "detailFragment");
                    ft.commitAllowingStateLoss();
                    break;
                case "trending":
                    Bundle bundle = new Bundle();
                    bundle.putString("country", "us");
                    topfrg.setArguments(bundle);
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                    ft1.replace(R.id.rl, topfrg, "detailFragment");
                    ft1.commitAllowingStateLoss();
                    break;
                case "playlists":
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    ft2.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                    ft2.replace(R.id.rl, playlistfrg, "detailFragment");
                    ft2.commit();
                    break;
                case "downloaded":
                    if (PermissionHandler.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage", 1000)) {
                        FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                        ft4.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                        ft4.replace(R.id.rl, downloadfrg, "detailFragment");
                        ft4.commit();
                    }
                    break;
                case "settings":
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    ft3.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                    ft3.replace(R.id.rl, settingsfrg, "detailFragment");
                    ft3.commit();                                                   
                    break;
            }
        }

        // TODO: 30. 4. 2019 - UPRAVIT NA WEBE ZANRE (ID)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
                interstitialAd.show();
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

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        if (!no_ads) {
            Random r = new Random();
            int i1 = r.nextInt(4 - 1) + 1;
            if (i1 == 1)
                interstitialAd.loadAd();
        }


        navigation.enableShiftingMode(false);
        navigation.enableAnimation(true);
        navigation.setTypeface(face);
        navigation.enableItemShiftingMode(false);
        navigation.setTextSize(11);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this, Search.class);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    
    public void test(View v){
        FragmentManager manager = getSupportFragmentManager();
        manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.rl, new FavouriteFrag()).commit();
    }

}