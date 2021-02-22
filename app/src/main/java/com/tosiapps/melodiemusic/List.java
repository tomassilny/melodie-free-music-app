package com.tosiapps.melodiemusic;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import androidx.fragment.app.FragmentManager;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class List extends AppCompatActivity {
    ListView listView;
    ProgressBar progressBar;
    TextView box_title;
    ImageView header_icon, bg;
    Spinner spin;
    Button play_all;
    ArrayList<String> tracks = new ArrayList<String>();

    ArrayList<String> songs = new ArrayList<String>();
    String [] playlist_names_r, titles_r, thumbs_r, genres_r, durations_r, id_r;

    String titles[], thumbs[], durations[], genres[], ids[], country_name = "";

    String[] countries, countries_codes = {"sk", "cz", "us", "pl", "hu", "de", "ca", "es"};


    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    private static String url = "";

    ArrayList<HashMap<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        setContentView(R.layout.activity_list);
        box_title = (TextView)findViewById(R.id.box_title);
        spin = (Spinner) findViewById(R.id.spinner);
        header_icon = (ImageView)findViewById(R.id.header_icon);
        bg = (ImageView)findViewById(R.id.bg);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        listView = (ListView)findViewById(R.id.listView);
        play_all = findViewById(R.id.play_all_btn);
        listView.setFooterDividersEnabled(false);

        list = new ArrayList<>();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String country = extras.getString("country");
        String playlist_name = extras.getString("playlist_name");


        if (extras.containsKey("playlist_name")){
            play_all.setVisibility(View.VISIBLE);
            box_title.setText(playlist_name);


            spin.setVisibility(View.INVISIBLE);
            header_icon.setImageResource(R.drawable.playlist_violet);

            TinyDB tinyDB = new TinyDB(this);
            songs = tinyDB.getListString("playlists_songs");
            int songs_in_playlist = 0;

            int first_song = 0;
            for (int i = 0; i < songs.size(); i++){
                String[] help_array = songs.get(i).split(";");
                if (help_array[0].equals(playlist_name)){
                    if (first_song != 0)
                        Picasso.get().load(help_array[3]).into(bg);
                    first_song++;
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
                    if (help_array[0].equals(playlist_name)){
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

            listView.setAdapter(new ListViewAdapter(getApplicationContext(), tracks, playlist_name));
            progressBar.setVisibility(View.INVISIBLE);
        }

        if (extras.containsKey("search")){
            String q = extras.getString("search");
            box_title.setText(getString(R.string.search_for) + " " + q);
            spin.setVisibility(View.INVISIBLE);
            header_icon.setImageResource(R.drawable.search_violet);

            url = "https://wawier.com/melodie/yt_search.php?count=50&q=" + q;
            new GetSongs().execute();
        }


        if (extras.containsKey("country")){
            url = "https://wawier.com/melodie/top.php?count=20&country=" + country;
            countries = getResources().getStringArray(R.array.countries);
            country_name = countries[Arrays.asList(countries_codes).indexOf(country)];
            spin.setSelection(Arrays.asList(countries_codes).indexOf(country));

            // SPINNER - COUNTRIES
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if (!selectedItem.equals(country_name)){
                        country_name = countries[position];
                        url = "https://wawier.com/melodie/top.php?count=20&country=" + countries_codes[position];
                        new GetSongs().execute();
                    }
                }
                public void onNothingSelected(AdapterView<?> parent)
                {

                }
            });
        }

        play_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Player.class);
                intent.putExtra("id", id_r[0]);
                intent.putExtra("title", titles_r[0]);
                intent.putExtra("playlist", playlist_name);
                startActivity(intent);
            }
        });

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = new Intent(List.this, MainActivity.class);
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        intent.putExtra("fragment", "home");
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.navigation_top:
                        intent.putExtra("fragment", "trending");
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.navigation_playlist:
                        intent.putExtra("fragment", "playlists");
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.navigation_downloaded:
                        intent.putExtra("fragment", "downloaded");
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.navigation_settings:
                        intent.putExtra("fragment", "settings");
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;
            }
        };

        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/proxima.otf");
        BottomNavigationViewEx navigation = (BottomNavigationViewEx) findViewById(R.id.bnve);
        navigation.enableShiftingMode(false);
        navigation.enableAnimation(true);
        navigation.setTypeface(face);
        navigation.setTextSize(11);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (extras.containsKey("country"))
            new GetSongs().execute();


            // hide download
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
                finish();

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private class GetSongs extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            songs.clear();
            list.clear();
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("songs");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String title = c.getString("title");
                        String thumb = c.getString("thumb");
                        String genres = c.getString("genres");
                        String duration = c.getString("duration");
                        String id = c.getString("id");

                        // tmp hash map for single contact
                        HashMap<String, String> song = new HashMap<>();

                        // adding each child node to HashMap key => value
                        song.put("title", title);
                        song.put("thumb", thumb);
                        song.put("genres", genres);
                        song.put("duration", duration);
                        song.put("id", id);

                        // adding contact to contact list
                        list.add(song);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (progressBar.getVisibility() == View.VISIBLE)
                progressBar.setVisibility(View.INVISIBLE);
            /**
             * Updating parsed JSON data into ListView
             * */
            titles = new String[list.size()];
            thumbs = new String[list.size()];
            genres = new String[list.size()];
            ids = new String[list.size()];
            durations = new String[list.size()];

            tracks.clear();

            for (int i = 0; i < list.size(); i++){
                titles[i] = String.valueOf(list.get(i).get("title"));
                thumbs[i] = String.valueOf(list.get(i).get("thumb"));
                genres[i] = String.valueOf(list.get(i).get("genres"));
                durations[i] = String.valueOf(list.get(i).get("duration"));
                ids[i] = String.valueOf(list.get(i).get("id"));
                tracks.add(ids[i] + ";" + titles[i] + ";" + thumbs[i] + ";" + genres[i]);
            }
            try {
                Picasso.get().load(thumbs[0]).into(bg);

            }catch (IndexOutOfBoundsException e){

            }
            listView.setAdapter(new ListViewAdapter(List.this, tracks, null));
        }

    }

    public void test(View v){
        FragmentManager manager = getSupportFragmentManager();
        manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.rl, new FavouriteFrag()).commit();
    }
}
