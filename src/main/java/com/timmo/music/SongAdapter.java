package com.timmo.music;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.util.ArrayList;

class SongAdapter extends BaseAdapter {

    private final ArrayList<Song> songs;
    private final LayoutInflater songInf;
    Context context;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return songs.size();
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
        //map to song layout
        @SuppressLint("ViewHolder") RelativeLayout songLay = (RelativeLayout) songInf.inflate(R.layout.song, parent, false);
        //get title and artist views
        TextView song_title = (TextView) songLay.findViewById(R.id.song_title);
        TextView song_artist = (TextView) songLay.findViewById(R.id.song_artist);
        TextView song_album = (TextView) songLay.findViewById(R.id.song_album);
        //ImageView song_art = (ImageView) songLay.findViewById(R.id.song_art);
        //get song using position
        Song currSong = songs.get(position);
        //get title and artist strings
        song_title.setText(currSong.getTitle());
        song_artist.setText(currSong.getArtist());
        song_album.setText(currSong.getAlbum());

        //Long albumId = Long.valueOf(currSong.getArt());
        //Bitmap art = getAlbumart(albumId);
        //song_art.setImageBitmap(art);

        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

    public Bitmap getAlbumart(Long album_id) {
        Bitmap bm = null;
        try {
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
            Log.e("GET ALBUM ART", "Error getting album art", e);
        }
        return bm;
    }

}