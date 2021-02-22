package com.tosiapps.melodiemusic;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.*;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class GridViewAdapter extends BaseAdapter{
    String[] names_r;
    Context context;
    ArrayList<String> playlists = new ArrayList<String>();
    ArrayList<String> playlists_songs = new ArrayList<>();

    private static LayoutInflater inflater=null;
    public GridViewAdapter(Context mainActivity, String[] names) {
        names_r = names;
        context=mainActivity;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }



    @Override
    public int getCount() {
        return names_r.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView title;
        ImageView thumb, more;
        TextView second_txt;
        RelativeLayout rl;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        final ImageView more, like;

        View rowView = null;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.gridviewstyle, parent, false);
        }else{
            rowView = convertView;
        }
        final Holder holder=new Holder();

        final TinyDB tinydb = new TinyDB(context);

        holder.title=(TextView) rowView.findViewById(R.id.title);
        holder.second_txt=(TextView)rowView.findViewById(R.id.second_txt);
        holder.thumb=(ImageView) rowView.findViewById(R.id.thumb);
        holder.rl = rowView.findViewById(R.id.rl);
        holder.more = rowView.findViewById(R.id.more);

        Typeface face = Typeface.createFromAsset(context.getAssets(),
                "fonts/proxima.otf");
        holder.title.setTypeface(face);
        holder.second_txt.setTypeface(face);

        playlists = tinydb.getListString("playlists");
        playlists_songs = tinydb.getListString("playlists_songs");

        final String this_name = playlists.get(position);
        String thumb_img = null;

        int count_same_playlists = 0;
        for (int i = 0; i < playlists_songs.size(); i++){
            String[] splitted = playlists_songs.get(i).split(";");
            if (splitted[0].equals(this_name)) {
                thumb_img = splitted[3];
                count_same_playlists++;
            }
        }

        holder.title.setText(this_name);
        if (count_same_playlists == 1)
            holder.second_txt.setText(count_same_playlists + " " + context.getString(R.string.track));
        else
            holder.second_txt.setText(count_same_playlists + " " + context.getString(R.string.tracks));

        if (Locale.getDefault().getLanguage() == "sk" && count_same_playlists > 4)
            holder.second_txt.setText(count_same_playlists + " " + context.getString(R.string.tracks2));




        if (thumb_img == null){
            Picasso.get().load(R.drawable.ic_play).into(holder.thumb);
        }else{
            Picasso.get().load(Uri.parse(thumb_img)).into(holder.thumb);
        }

        holder.rl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("playlist_name", this_name);
                ListFrag fragobj = new ListFrag();
                fragobj.setArguments(bundle);
                FragmentTransaction ft = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                ft.replace(R.id.rl, fragobj, "detailFragment");
                ft.commit();
            }
        });

        final View finalRowView = rowView;
        holder.more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate(R.menu.playlistmenu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.delete){
                            playlists = tinydb.getListString("playlists");
                            playlists_songs = tinydb.getListString("playlists_songs");
                            playlists.remove(this_name);
                            for (int i = 0; i < playlists_songs.size(); i++){
                                String[] splitted = playlists_songs.get(i).split(";");
                                if (splitted[0].equals(this_name)) {
                                    playlists_songs.remove(i);
                                }
                            }
                            tinydb.putListString("playlists", playlists);
                            finalRowView.setVisibility(View.GONE);
                        }
                        if (menuItem.getItemId() == R.id.play){
                            Bundle bundle = new Bundle();
                            bundle.putString("playlist_name", this_name);
                            ListFrag fragobj = new ListFrag();
                            fragobj.setArguments(bundle);
                            FragmentTransaction ft = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
                            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                            ft.replace(R.id.rl, fragobj, "detailFragment");
                            ft.commit();
                        }
                        return false;
                    }
                });
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
}