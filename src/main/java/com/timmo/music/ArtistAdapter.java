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

class ArtistAdapter extends BaseAdapter {

    private final ArrayList<Artist> artists;
    private final LayoutInflater artistInf;
    Context context;

    public ArtistAdapter(Context c, ArrayList<Artist> theArtists) {
        artists = theArtists;
        artistInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return artists.size();
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
        //map to artist layout
        @SuppressLint("ViewHolder") RelativeLayout artistLay = (RelativeLayout) artistInf.inflate(R.layout.artist, parent, false);
        //get title and artist views
        TextView artist_artist = (TextView) artistLay.findViewById(R.id.artist_artist);
        //ImageView artist_art = (ImageView) artistLay.findViewById(R.id.artist_art);
        Artist currArtist = artists.get(position);
        artist_artist.setText(currArtist.getArtist());

        //Long artistId = Long.valueOf(currArtist.getArt());
        //Bitmap art = getArt(artistId);
        //artist_art.setImageBitmap(art);

        //set position as tag
        artistLay.setTag(position);
        return artistLay;
    }

    public Bitmap getArt(Long artist_id) {
        Bitmap bm = null;
        try {
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, artist_id);

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