package com.timmo.music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

class GenreAdapter extends BaseAdapter {

    private final ArrayList<Genre> genres;
    private final LayoutInflater genreInf;
    Context context;

    public GenreAdapter(Context c, ArrayList<Genre> theGenres) {
        genres = theGenres;
        genreInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return genres.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to genre layout
        @SuppressLint("ViewHolder") RelativeLayout genreLay = (RelativeLayout) genreInf.inflate(R.layout.genre, parent, false);
        //get title and genre views
        TextView genre_genre = (TextView) genreLay.findViewById(R.id.genre_genre);
        //ImageView genre_art = (ImageView) genreLay.findViewById(R.id.genre_art);
        Genre currGenre = genres.get(position);
        genre_genre.setText(currGenre.getGenre());

        //Long genreId = Long.valueOf(currGenre.getArt());
        //Bitmap art = getArt(genreId);
        //genre_art.setImageBitmap(art);

        //set position as tag
        genreLay.setTag(position);
        return genreLay;
    }

}