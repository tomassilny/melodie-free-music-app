package com.tosiapps.melodiemusic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class ListFrag extends Fragment {
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

    // EDIT AJ V PHP
    String[] countries, countries_codes = {"sk", "cz", "us", "pl", "hu", "de", "ca", "es"};


    private String TAG = "List";
    private ProgressDialog pDialog;

    private static String url = "";

    ArrayList<HashMap<String, String>> list;

    public ListFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_list, container, false);


        box_title = rootview.findViewById(R.id.box_title);
        spin =  rootview.findViewById(R.id.spinner);
        header_icon = rootview.findViewById(R.id.header_icon);
        bg = rootview.findViewById(R.id.bg);
        progressBar = rootview.findViewById(R.id.progressBar);
        listView = rootview.findViewById(R.id.listView);
        play_all = rootview.findViewById(R.id.play_all_btn);
        listView.setFooterDividersEnabled(false);

        list = new ArrayList<>();

        Bundle arguments = getArguments();
        String country = arguments.getString("country");
        String playlist_name = arguments.getString("playlist_name");


        if (arguments.containsKey("playlist_name")){
            play_all.setVisibility(View.VISIBLE);
            box_title.setText(playlist_name);


            spin.setVisibility(View.INVISIBLE);
            header_icon.setImageResource(R.drawable.playlist_violet);

            TinyDB tinyDB = new TinyDB(getContext());
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

            listView.setAdapter(new ListViewAdapter(getContext(), tracks, playlist_name));
            progressBar.setVisibility(View.INVISIBLE);
        }

        if (arguments.containsKey("search")){
            String q = arguments.getString("search");
            box_title.setText(getString(R.string.search_for) + " " + q);
            spin.setVisibility(View.INVISIBLE);
            header_icon.setImageResource(R.drawable.search_violet);

            url = "https://wawier.com/melodie/searchresults.php?count=20&q=" + q;
            new GetSongs().execute();
        }


        if (arguments.containsKey("country")){
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
                Intent intent = new Intent(getContext(), Player.class);
                intent.putExtra("id", id_r[0]);
                intent.putExtra("title", titles_r[0]);
                intent.putExtra("playlist", playlist_name);
                startActivity(intent);
            }
        });
        if (arguments.containsKey("country"))
            new GetSongs().execute();

        return rootview;
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


                }
            } else {

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
            Picasso.get().load(thumbs[0]).into(bg);
            listView.setAdapter(new ListViewAdapter(getContext(), tracks, null));
        }

    }
}
