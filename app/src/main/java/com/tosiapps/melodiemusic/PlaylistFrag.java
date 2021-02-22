package com.tosiapps.melodiemusic;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFrag extends Fragment {
    GridView gridView;
    RelativeLayout no_playlists;
    ArrayList<String> playlists = new ArrayList<String>();
    ArrayList<String> playlists_songs = new ArrayList<>();

    public PlaylistFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_playlist, container, false);

        no_playlists = rootview.findViewById(R.id.no_playlists);
        gridView = rootview.findViewById(R.id.gv);
        TinyDB tinyDB = new TinyDB(getContext());
        playlists = tinyDB.getListString("playlists");

        // check for empty playlists and remove them
        int count_same_playlists = 0;
        for (int j = 0; j < playlists.size(); j++){
            for (int i = 0; i < playlists_songs.size(); i++){
                String[] splitted = playlists_songs.get(i).split(";");
                if (splitted[0].equals(playlists.get(0))) {
                    count_same_playlists++;
                }
            }
            if (count_same_playlists == 0){
                playlists.remove(j);
            }
            count_same_playlists = 0;
        }

        playlists = tinyDB.getListString("playlists");


        String[] playlists_arr = new String[playlists.size()];


        for(int i = 0; i < playlists.size(); i++){
            playlists_arr[i] = playlists.get(i).toString();
        }

        if (playlists.size() <= 0)
            no_playlists.setVisibility(View.VISIBLE);

        gridView.setAdapter(new GridViewAdapter(getContext(), playlists_arr));

        return rootview;
    }

}
