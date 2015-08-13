package com.timmo.music;

import android.content.ContentResolver;
import android.content.CursorLoader;
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

public class ArtistsFragment extends Fragment {

    public ArtistsFragment() {
    }

    // region GlobalVars
    private ArrayList<Artist> artistList;
    View view;
    ArtistAdapter artistAdt;
    GridView gvArtists;
    String chosenArtist;
    Uri uri;
    ContentResolver musicResolver;
    // endregion

    // region CreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_artists, container, false);

        musicResolver = getActivity().getContentResolver();

        gvArtists = (GridView) view.findViewById(R.id.gvArtists);

        //instantiate list
        artistList = new ArrayList<>();

        //get artists from device
        getArtistList();

        // Group ArrayList..
        artistList = new ArrayList<>(new HashSet<>(artistList));

        //sort alphabetically by title
        Collections.sort(artistList, new Comparator<Artist>() {
                    public int compare(Artist a, Artist b) {
                        return a.getArtist().compareTo(b.getArtist());
                    }
                }
        );

        //create and set adapter
        artistAdt = new ArtistAdapter(getActivity(), artistList);
        gvArtists.setAdapter(artistAdt);

        return view;
    }
    // endregion

    // region GetOriginalList
    void getArtistList() {
        //retrieve artist info
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int artColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            //add artists to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisArt = musicCursor.getString(artColumn);

                artistList.add(new Artist(thisId, thisArtist, thisArt));
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }
    // endregion

    int currentArtist;

    // region ArtistPicked
    public void artistPicked(View v) {
        //instantiate list
        artistList = new ArrayList<>();

        //get artist
        uri = MediaStore.Audio.Artists.Albums.getContentUri("external", Integer.parseInt(v.getTag().toString()));

        currentArtist = Integer.parseInt(v.getTag().toString());

        getArtist();

        // Group ArrayList..
        artistList = new ArrayList<>(new HashSet<>(artistList));

        //sort alphabetically by title
/*
        Collections.sort(artistList, new Comparator<Artist>() {
                    public int compare(Artist a, Artist b) {
                        return a.getArtist().compareTo(b.getArtist());
                    }
                }
        );
*/

        //create and set adapter
        artistAdt = new ArtistAdapter(getActivity(), artistList);
        gvArtists.setAdapter(artistAdt);
    }

    public void getArtist() {
        //retrieve artist info
        //ContentResolver musicResolver = getActivity().getContentResolver();
        //Uri musicUri = MediaStore.Audio.Artists.Albums.getContentUri("external", Integer.parseInt(view.getTag().toString()));
        Cursor artistNameCursor = getActivity().managedQuery(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.ARTIST},
                MediaStore.Audio.Media._ID + " == " + currentArtist + " ",
                null,
                null);
        // Cursor musicCursor = musicResolver.query(uri, null, null, null, null);

        if (artistNameCursor != null && artistNameCursor.moveToFirst()) {
            //get columns
            int idColumn = artistNameCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = artistNameCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int artColumn = artistNameCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            //add artists to list
            do {
                long thisId = artistNameCursor.getLong(idColumn);
                String thisArtist = artistNameCursor.getString(artistColumn);
                String thisArt = artistNameCursor.getString(artColumn);

                artistList.add(new Artist(thisId, thisArtist, thisArt));
            }
            while (artistNameCursor.moveToNext());
            artistNameCursor.close();
        }
    }
    // endregion
}