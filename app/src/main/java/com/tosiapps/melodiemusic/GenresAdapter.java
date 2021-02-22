package com.tosiapps.melodiemusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GenresAdapter extends BaseAdapter {

    private Integer[] genres_images = {
      R.drawable.pop_square,
      R.drawable.rap_square,
            R.drawable.rock_square,
            R.drawable.country_square,
            R.drawable.children_square,
            R.drawable.classical_square,
            R.drawable.electro_square,
            R.drawable.soul_square,
            R.drawable.latin_square,
    };

    private String[] genreLabels;
    private Context context;
    private LayoutInflater thisInflater;

    public GenresAdapter(Context context, String[] labs){
        this.context = context;
        this.thisInflater = LayoutInflater.from(context);
        this.genreLabels = labs;
    }

    @Override
    public int getCount() {
        return genres_images.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = thisInflater.inflate(R.layout.grid_genre_item, viewGroup, false);

            TextView genreHeading = view.findViewById(R.id.genreHeading);
            ImageView genreImage = view.findViewById(R.id.genreImage);

            genreHeading.setText(genreLabels[i]);
            genreImage.setImageResource(genres_images[i]);
        }
        return view;
    }
}
