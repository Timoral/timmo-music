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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.util.ArrayList;

class AlbumAdapter extends BaseAdapter {

    private final ArrayList<Album> albums;
    private final LayoutInflater albumInf;
    Context context;

    public AlbumAdapter(Context c, ArrayList<Album> theAlbums) {
        albums = theAlbums;
        albumInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return albums.size();
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
        //map to album layout
        @SuppressLint("ViewHolder") RelativeLayout albumLay = (RelativeLayout) albumInf.inflate(R.layout.album, parent, false);
        //get title and album views
        TextView album_album = (TextView) albumLay.findViewById(R.id.album_album);
        ImageView album_art = (ImageView) albumLay.findViewById(R.id.album_art);
        Album currAlbum = albums.get(position);
        album_album.setText(currAlbum.getAlbum());

        Long albumId = Long.valueOf(currAlbum.getArt());
        Bitmap art = getArt(albumId);
        album_art.setImageBitmap(art);

        //set position as tag
        albumLay.setTag(position);
        return albumLay;
    }

    public Bitmap getArt(Long album_id) {
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