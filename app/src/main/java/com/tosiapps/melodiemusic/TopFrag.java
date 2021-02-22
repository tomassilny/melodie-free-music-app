package com.tosiapps.melodiemusic;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class TopFrag extends Fragment {
    ListView listView;
    ProgressBar progressBar;
    TextView box_title;
    ImageView header_icon, bg;
    Spinner spin;
    ArrayList<String> tracks = new ArrayList<String>();
    GetSongs songs_task;

    ArrayList<String> songs = new ArrayList<String>();
    String [] playlist_names_r, titles_r, thumbs_r, genres_r, durations_r, id_r;

    String titles[], thumbs[], durations[], genres[], ids[], country_name = "";

    String[] countries, genres_this;
    public String[] countries_codes = {"us", "ca", "sk", "cz", "pl", "hu", "de", "es"};
    String[] genres_def = {"pop", "rap", "rock", "country", "kids", "classic", "electro", "soul", "latin"};

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    private static String url = "";

    ArrayList<HashMap<String, String>> list;

    public TopFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_top, container, false);


        progressBar = (ProgressBar)rootview.findViewById(R.id.progressBar);
        listView = (ListView)rootview.findViewById(R.id.listView);
        spin = rootview.findViewById(R.id.spinner);
        bg = rootview.findViewById(R.id.bg);
        box_title = rootview.findViewById(R.id.box_title);
        header_icon = rootview.findViewById(R.id.header_icon);

        list = new ArrayList<>();

        Bundle arguments = getArguments();
        String country = arguments.getString("country");
        String genre = arguments.getString("genre");

        if (country != null) {

            url = "https://wawier.com/melodie/top.php?count=50&country=" + country;
            countries = getResources().getStringArray(R.array.countries);
            country_name = countries[Arrays.asList(countries_codes).indexOf(country)];
            spin.setSelection(Arrays.asList(countries_codes).indexOf(country));
        }else{
            genres_this = getResources().getStringArray(R.array.genres_headings);
            url = "https://wawier.com/melodie/genres.php?genre=" + genres_def[Arrays.asList(genres_this).indexOf(genre)];
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.genres_headings, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin.setAdapter(adapter);
            spin.setSelection(Arrays.asList(genres_this).indexOf(genre));
            box_title.setText(getString(R.string.genres));
            header_icon.setImageResource(R.drawable.cd);
        }

        songs_task = (GetSongs) new GetSongs().execute();
            // SPINNER - COUNTRIES
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if (country != null){
                        if (!selectedItem.equals(country_name)){
                            country_name = countries[position];
                            url = "https://wawier.com/melodie/top.php?count=50&country=" + countries_codes[position];
                            new GetSongs().execute();
                        }
                    }else{
                        if (!selectedItem.equals(genre)){
                            url = "https://wawier.com/melodie/genres.php?genre=" + genres_def[position];
                            new GetSongs().execute();
                        }
                    }
                }
                public void onNothingSelected(AdapterView<?> parent)
                {

                }
            });


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
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");

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
            }catch (Exception e){
                Log.d("Img error", "IMG ERROR");
            }

            int k = 0, deleteIndex = 0;
            int n = tracks.size();
            for (k = 0; k < n; k++){
                if (tracks.get(k).contains("null")){
                    deleteIndex = k;
                    break;
                }
            }

            if (deleteIndex != 0) {
                for (k = deleteIndex; k < tracks.size(); ) {
                    tracks.remove(k);
                }
            }



            listView.setAdapter(new ListViewAdapter(getActivity(), tracks, null));


        }

    }

    public void cancelAllTasks(){
        if (songs_task != null)
            songs_task.cancel(true);
    }

}
