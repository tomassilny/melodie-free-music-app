package com.tosiapps.melodiemusic;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tosiapps.melodiemusic.YouTubeDownloader.PermissionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ListViewAdapter extends BaseAdapter{
    Context context;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    String playlist = "";
    ArrayList<String> saved_songs = new ArrayList<String>();
    ArrayList<String> songs = new ArrayList<String>();
    ArrayList<String> tracks;
    ArrayList<String> playlists = new ArrayList<>();
    ArrayList<String> playlists_songs = new ArrayList<>();
    TinyDB tinydb;
    private DatabaseReference mDatabase;

    private static LayoutInflater inflater=null;
    public ListViewAdapter(Context mainActivity, ArrayList tracks_a, String playlist) {
        // TODO Auto-generated constructor stub
        tracks = tracks_a;
        context=mainActivity;
        this.playlist = playlist;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView title;
        ImageView thumb;
        TextView genres;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        String titles_r, thumbs_r, genres_r, durations_r, id_r;


        String[] help_array = tracks.get(position).split(";");
        id_r = help_array[0];
        titles_r = help_array[1];
        thumbs_r = help_array[2];
        genres_r = help_array[3];

        final ImageView more, like, delete, download;

        // TODO Auto-generated method stub
        View rowView = null;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.listviewstyle, parent, false);
        }else{
            rowView = convertView;
        }
        final Holder holder=new Holder();

        tinydb = new TinyDB(context);

        holder.title=(TextView) rowView.findViewById(R.id.title);
        holder.genres=(TextView)rowView.findViewById(R.id.genres);
        holder.thumb=(ImageView) rowView.findViewById(R.id.thumb);
        more = (ImageView)rowView.findViewById(R.id.more);
        like = rowView.findViewById(R.id.like);
        download = rowView.findViewById(R.id.download);
        delete = rowView.findViewById(R.id.delete);
        delete.setImageResource(R.drawable.delete);
        more.setImageResource(R.drawable.more);
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

        if (playlist != null)
            delete.setVisibility(View.VISIBLE);

        Typeface face = Typeface.createFromAsset(context.getAssets(),
                "fonts/proxima.otf");
        holder.title.setTypeface(face);
        holder.genres.setTypeface(face);


        saved_songs = tinydb.getListString("saved_songs");

        final String song_object = id_r + ";" + titles_r + ";" + thumbs_r + ";" + genres_r;

        if (saved_songs.contains(song_object)){
            like.setImageResource(R.drawable.heart_green);
        }else{
            like.setImageResource(R.drawable.heart_btn);
        }

        download.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doDownloadVideo("https://youtube.com/watch?v=" + id_r);
            }
        });

        like.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saved_songs.contains(song_object)){
                    like.setImageResource(R.drawable.heart_btn);
                    saved_songs.remove(song_object);
                }else{
                    like.setImageResource(R.drawable.heart_green);
                    saved_songs.add(song_object);
                }
                tinydb.putListString("saved_songs", saved_songs);
            }
        });
        

        more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate(R.menu.listmenu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.play){
                            Intent intent = new Intent(context, Player.class);
                            intent.putExtra("thumb", thumbs_r);
                            intent.putExtra("id", id_r);
                            intent.putExtra("title", titles_r);
                            intent.putExtra("genres", genres_r);
                            if (playlist != null)
                                intent.putExtra("playlist", playlist);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                        if (menuItem.getItemId() == R.id.add_to_playlist){
                            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
                            builderSingle.setTitle(R.string.playlists);
                            builderSingle.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            playlists = tinydb.getListString("playlists");
                            playlists_songs = tinydb.getListString("playlists_songs");

                            ArrayAdapter<String> deviceAdapter;
                            deviceAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
                            deviceAdapter.add(context.getString(R.string.create_new_playlist));
                            if (playlists.size() > 0){
                                for(int i = 0; i < playlists.size(); i++){
                                    deviceAdapter.add(playlists.get(i));
                                }
                            }

                            builderSingle.setAdapter(deviceAdapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == 0){
                                        createPlaylistDialog(id_r, titles_r, thumbs_r, genres_r);
                                    }else{
                                        int start_index = 0;
                                        for (int j = 0; j < playlists_songs.size(); j++){
                                            if (playlists_songs.get(j).split(";")[0].equals(playlists.get(i - 1))){
                                                start_index = j;
                                                break;
                                            }
                                        }
                                        String[] splitted = playlists_songs.get(start_index).split(";");

                                        if (Arrays.asList(splitted).contains(titles_r)){
                                            Toast.makeText(context, R.string.track_already_exist_in_this_playlist, Toast.LENGTH_SHORT).show();
                                        }else {
                                            playlists_songs.add(playlists.get(i - 1) + ";" + id_r + ";" + titles_r + ";" + thumbs_r + ";" + genres_r);
                                            tinydb.putListString("playlists_songs", playlists_songs);
                                            Toast.makeText(context, R.string.track_successfully_added_to_playlist, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                          builderSingle.show();
                        }
                        return false;
                    }
                });
            }
        });

        final View finalRowView = rowView;
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                songs = tinydb.getListString("playlists_songs");
                songs.remove(playlist + ";" + song_object);
                tracks.remove(song_object);
                tinydb.putListString("playlists_songs", songs);
                notifyDataSetChanged();
            }
        });

        holder.title.setText(titles_r);
        holder.genres.setText(genres_r);

       // Toast.makeText(context, "URL: " + thumbs_r[position], Toast.LENGTH_LONG).show();

        if (thumbs_r == null){
            Picasso.get().load(R.drawable.ic_play).into(holder.thumb);
        }else{
            Picasso.get().load(Uri.parse(thumbs_r)).into(holder.thumb);
        }

        rowView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                    Toast.makeText(context, context.getString(R.string.permission), Toast.LENGTH_LONG).show();
                    askPermission();
                } else {

                    if (isMyServiceRunning(FloatingViewService.class)) {
                        context.stopService(new Intent(context, FloatingViewService.class));
                        Thread.interrupted();
                    }
                    Intent intent = new Intent(context, Player.class);
                    intent.putExtra("thumb", thumbs_r);
                    intent.putExtra("id", id_r);
                    intent.putExtra("title", titles_r);
                    intent.putExtra("genres", genres_r);
                    if (playlist != null)
                        intent.putExtra("playlist", playlist);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

        return rowView;
    }
    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void createPlaylistDialog(String id, String title, String thumb, String genres){
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(R.string.create_new_playlist);

        final EditText input = new EditText(context);
        input.setWidth(10);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builderSingle.setView(input);

        builderSingle.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String txt = input.getText().toString();
                createPlaylist(txt, id, title, thumb, genres);
            }
        });
        builderSingle.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builderSingle.show();
    }

    public void createPlaylist(String name, String id, String title, String thumb, String genres){
        playlists = tinydb.getListString("playlists");
        playlists.add(name);
        if (playlists.equals(name)){
            Toast.makeText(context, R.string.playlist_with_name + " " + name + " " + R.string.already_exist, Toast.LENGTH_SHORT).show();
        }else{
            tinydb.putListString("playlists", playlists);
            playlists_songs.add(name + ";" + id + ";" + title + ";" + thumb + ";" + genres);
            tinydb.putListString("playlists_songs", playlists_songs);
            if (name.matches(""))
                Toast.makeText(context, R.string.please_fill_playlist_name, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Playlist " + name  + " " + context.getString(R.string.successfully_created), Toast.LENGTH_LONG).show();
        }
    }
    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        ((Activity)context).startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    private void doDownloadVideo(String link)
    {
        if (PermissionHandler.isPermissionGranted((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage", 1000)) {
            DownloadActivity downloadActivity = new DownloadActivity();
            downloadActivity.getYoutubeDownloadUrl(context, link);
        }
    }
}