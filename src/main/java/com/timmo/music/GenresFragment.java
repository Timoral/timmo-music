package com.timmo.music;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GenresFragment extends Fragment {

    private ArrayList<Genre> genreList;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_genres, container, false);

        GridView gvGenres = (GridView) view.findViewById(R.id.gvGenres);

        //instantiate list
        genreList = new ArrayList<>();

        //get genres from device
        getGenreList();

        //sort alphabetically by title
        Collections.sort(genreList, new Comparator<Genre>() {
                    public int compare(Genre a, Genre b) {
                        return a.getGenre().compareTo(b.getGenre());
                    }
                }
        );

        //create and set adapter
        GenreAdapter genreAdt = new GenreAdapter(getActivity(), genreList);
        gvGenres.setAdapter(genreAdt);

        return view;
    }

    void getGenreList() {
        //retrieve song info
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int genreColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Genres._ID);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisGenre = musicCursor.getString(genreColumn);

                genreList.add(new Genre(thisId, thisGenre));
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }

}