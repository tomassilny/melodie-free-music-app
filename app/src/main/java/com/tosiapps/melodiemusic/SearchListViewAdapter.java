package com.tosiapps.melodiemusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.*;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SearchListViewAdapter extends BaseAdapter{
    ArrayList<String> queries;
    boolean deletable;
    Context context;
    private static LayoutInflater inflater=null;
    public SearchListViewAdapter(Context mainActivity, ArrayList<String> recent_queries, boolean deletable) {
        // TODO Auto-generated constructor stub
        queries = recent_queries;
        this.deletable = deletable;
        context=mainActivity;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return queries.size();
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
        ImageView delete;
        ImageView img;
        TextView title;
    }
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // TODO Auto-generated method stub
        View rowView = null;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.search_row_style, parent, false);
        }else{
            rowView = convertView;
        }
        final Holder holder=new Holder();


        holder.delete= rowView.findViewById(R.id.delete);
        holder.title = rowView.findViewById(R.id.title);
        holder.img = rowView.findViewById(R.id.fav);
        holder.delete.setImageResource(R.drawable.delete_white);

        if (deletable)
            holder.img.setImageResource(R.drawable.previous);
        else
            holder.img.setImageResource(R.drawable.search);


        Typeface face = Typeface.createFromAsset(context.getAssets(),
                "fonts/proxima.otf");

        holder.title.setTypeface(face);

        final TinyDB tinydb = new TinyDB(context);

        holder.delete.setOnClickListener(view -> {
            queries.remove(queries.get(position));
            tinydb.putListString("recent_queries", queries);
            notifyDataSetChanged();
        });

        holder.title.setText(queries.get(position));

        rowView.setOnClickListener(v -> {
            Intent intent = new Intent(context, List.class);
            intent.putExtra("search", queries.get(position));
            context.startActivity(intent);
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