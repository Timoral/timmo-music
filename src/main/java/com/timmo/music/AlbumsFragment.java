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
import java.util.HashSet;

public class AlbumsFragment extends Fragment {

    private ArrayList<Album> albumList;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_albums, container, false);

        GridView gvAlbums = (GridView) view.findViewById(R.id.gvAlbums);

        //instantiate list
        albumList = new ArrayList<>();

        //get albums from device
        getAlbumList();

        // Group ArrayList..
        albumList = new ArrayList<>(new HashSet<>(albumList));

        //sort alphabetically by title
        Collections.sort(albumList, new Comparator<Album>() {
                    public int compare(Album a, Album b) {
                        return a.getAlbum().compareTo(b.getAlbum());
                    }
                }
        );

        //create and set adapter
        AlbumAdapter albumAdt = new AlbumAdapter(getActivity(), albumList);
        gvAlbums.setAdapter(albumAdt);

        return view;
    }

    void getAlbumList() {
        //retrieve album info
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM);
            int artColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            //add albums to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisArt = musicCursor.getString(artColumn);

                albumList.add(new Album(thisId, thisAlbum, thisArt));
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }
}