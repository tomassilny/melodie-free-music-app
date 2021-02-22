package com.tosiapps.melodiemusic;


import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteFrag extends Fragment {
    ArrayList<String> saved_songs = new ArrayList<String>();
    ArrayList<String> tracks = new ArrayList<String>();
    String [] titles_r, thumbs_r, genres_r, durations_r, id_r;
    ProgressBar progressBar;
    RelativeLayout no_saved_track_layout;
    ListAdapter adapter;
    ListView savedListView;
    RecyclerView recyclerView;
    int limit = 10, max;
    boolean flag_loading = false, end = false;
    boolean limit_stop = false, first_time = true;

    public FavouriteFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview =  inflater.inflate(R.layout.fragment_favourite, container, false);
        //progressBar = rootview.findViewById(R.id.progressBar2);
        //progressBar.setVisibility(View.VISIBLE);
        no_saved_track_layout = rootview.findViewById(R.id.no_saved_tracks_layout);
        progressBar = rootview.findViewById(R.id.progressBar);

        TinyDB tinyDB = new TinyDB(getContext());
        saved_songs = tinyDB.getListString("saved_songs");


        savedListView = rootview.findViewById(R.id.listViewSaved);

        int title_size = 0;

        if (saved_songs.size() > 0) {
            new GetSongs().execute();
            title_size = saved_songs.size();
        }
        else {
            no_saved_track_layout.setVisibility(View.VISIBLE);
            title_size = 0;
        }

        TextView title = rootview.findViewById(R.id.box_title);
        title.setText(title.getText() + " (" + title_size + ")");

        return rootview;
    }


    private class GetSongs extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... voids) {

            max = saved_songs.size();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Collections.reverse(saved_songs);
            adapter = new ListViewAdapter(getContext(), saved_songs, null);
            savedListView.setAdapter(adapter);
        }

    }

}
