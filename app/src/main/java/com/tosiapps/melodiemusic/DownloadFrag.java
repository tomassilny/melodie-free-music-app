package com.tosiapps.melodiemusic;


import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadFrag extends Fragment {
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

    public DownloadFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview =  inflater.inflate(R.layout.fragment_download, container, false);
        //progressBar = rootview.findViewById(R.id.progressBar2);
        //progressBar.setVisibility(View.VISIBLE);
        no_saved_track_layout = rootview.findViewById(R.id.no_saved_tracks_layout);
        progressBar = rootview.findViewById(R.id.progressBar);

        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS + "/Melodie Music/").getAbsolutePath());

        String parentDirectory = downloadDir.toString();
        File dirFileObj = new File(parentDirectory);
        File[] files = dirFileObj.listFiles();

        for (int i = 0; i < files.length; i++){
            saved_songs.add(";" + files[i].getName() + ";p;p");
        }



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
            adapter = new ListViewAdapterDownload(getContext(), saved_songs, null);
            savedListView.setAdapter(adapter);
        }

    }

    public void getFilesFromDir(File filesFromSD) {

        File listAllFiles[] = filesFromSD.listFiles();

        if (listAllFiles != null && listAllFiles.length > 0) {
            for (File currentFile : listAllFiles) {
                if (currentFile.isDirectory()) {
                    getFilesFromDir(currentFile);
                } else {
                    if (currentFile.getName().endsWith("")) {
                        // File absolute path
                        Log.e("File path", currentFile.getAbsolutePath());
                        // File Name
                        Log.e("File path", currentFile.getName());

                    }
                }
            }
        }

    }



}
