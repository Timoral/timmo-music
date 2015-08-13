package com.timmo.music;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.astuetz.PagerSlidingTabStrip;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class MusicActivity extends ActionBarActivity implements MediaController.MediaPlayerControl, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, ViewPager.OnPageChangeListener {

    // region GlobalVars
    private ArrayList<Genre> genreList;
    private ArrayList<Artist> artistList;
    private ArrayList<Album> albumList;
    private ArrayList<Song> songList;

    private SharedPreferences sharedPreferences;
    private final String keySong = "Song";
    //private final String keyPos = "Pos";
    private boolean loaded = false;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    private boolean paused = false;

    private Handler handler;
    private Runnable run;

    private ViewSwitcher viewSwitcher;
    private ImageButton ibShuffle;
    private ImageButton ibRepeat;
    private ImageButton ibPlay;
    private ImageButton ibLibPlay;
    private TextView tvTitle;
    private TextView tvArtist;
    private TextView tvAlbum;
    private TextView tvLibTitle;
    private TextView tvLibArtist;
    private TextView tvLibAlbum;
    private ImageView ivArt3;
    private ImageView ivLibArt;
    private SeekBar sbProgress;
    private TextView tvProgress;
    private TextView tvTotal;
    private int prog;
    private boolean changingPos = false;

    private boolean isLib = false;

    private final int runSpeed = 50;

    PagerSlidingTabStrip tabs;
    private ViewPager pager;
    PagerAdapter pagerAdapter;

    ArtistsFragment artistsFragment;
    // endregion

    // region onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        String filename = "SavedData";
        sharedPreferences = getSharedPreferences(filename, MODE_PRIVATE);

        // Initialize the ViewPager and set an adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(1);

        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);

        artistsFragment = new ArtistsFragment();

        // region Views
        RelativeLayout rlLibControls = (RelativeLayout) findViewById(R.id.rlLibControls);

        viewSwitcher = (ViewSwitcher)

                findViewById(R.id.viewSwitcher);

        tvTitle = (TextView)

                findViewById(R.id.tvTitle);

        tvArtist = (TextView)

                findViewById(R.id.tvArtist);

        tvAlbum = (TextView)

                findViewById(R.id.tvAlbum);

        ibShuffle = (ImageButton)

                findViewById(R.id.ibShuffle);

        ivArt3 = (ImageView)

                findViewById(R.id.ivArt3);

        ivLibArt = (ImageView)

                findViewById(R.id.ivLibArt);

        ImageButton ibPrevious = (ImageButton) findViewById(R.id.ibPrevious);
        ibPlay = (ImageButton)

                findViewById(R.id.ibPlay);

        ImageButton ibNext = (ImageButton) findViewById(R.id.ibNext);
        ibRepeat = (ImageButton)

                findViewById(R.id.ibRepeat);

        sbProgress = (SeekBar)

                findViewById(R.id.sbProgress);

        tvProgress = (TextView)

                findViewById(R.id.tvProgress);

        tvTotal = (TextView)

                findViewById(R.id.tvTotal);

        tvLibTitle = (TextView)

                findViewById(R.id.tvLibTitle);

        tvLibArtist = (TextView)

                findViewById(R.id.tvLibArtist);

        tvLibAlbum = (TextView)

                findViewById(R.id.tvLibAlbum);

        ibLibPlay = (ImageButton)

                findViewById(R.id.ibLibPlay);

        ibShuffle.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibPlay.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibRepeat.setOnClickListener(this);

        sbProgress.setOnSeekBarChangeListener(this);

        ibLibPlay.setOnClickListener(this);

        rlLibControls.setOnClickListener(this);

        //instantiate list
        genreList = new ArrayList<>();
        artistList = new ArrayList<>();
        albumList = new ArrayList<>();
        songList = new ArrayList<>();

        //get list
        getGenreList();

        getArtistList();

        getAlbumList();

        getSongList();

        //GridView gvArtists = (GridView) findViewById(R.id.gvArtists);
        //GridView gvAlbums = (GridView) findViewById(R.id.gvAlbums);
        //ListView lvSongs = (ListView) findViewById(R.id.lvSongs);

        //sort alphabetically by title
        Collections.sort(songList, new Comparator<Song>()

                {
                    public int compare(Song a, Song b) {
                        return a.getTitle().compareTo(b.getTitle());
                    }
                }

        );
        //create and set adapter
        //SongAdapter songAdt = new SongAdapter(this, songList);
        // gvAlbums.setAdapter(songAdt);

        setViews();

        Tests();
        // endregion
    }
    // endregion

    // region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_library:
                SwitchViews();
                return true;
            case R.id.action_equaliser:
                //Intent eq = new Intent();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_exit:
                ExitApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // endregion

    // region onClick
    @Override
    public void onClick(View view) {
        handler.removeCallbacks(run);
        switch (view.getId()) {
            case R.id.rlLibControls:
                SwitchViews();
                break;
            case R.id.llArt:
                SwitchViews();
                break;
            case R.id.ibShuffle:
                SetShuffle();
                setViews();
                break;
            case R.id.ibPrevious:
                playPrev();
                break;
            case R.id.ibPlay:
                PlayPause();
                break;
            case R.id.ibNext:
                playNext();
                break;
            case R.id.ibRepeat:
                SetRepeat();
                break;
            case R.id.ibLibPlay:
                PlayPause();
                break;
            default:
                break;
        }
        handler.postDelayed(run, runSpeed);
    }
    // endregion

    // region OnPageChangeListener
    @Override
    public void onPageSelected(int position) {
        pager.setCurrentItem(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
    // endregion

    // region SeekBarOnChange
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        prog = progress;
        int progHrs = (int) TimeUnit.MILLISECONDS.toHours(prog) % 24;
        int progMins = (int) TimeUnit.MILLISECONDS.toMinutes(prog) % 60;
        int progSecs = (int) TimeUnit.MILLISECONDS.toSeconds(prog) % 60;
        String sPosHrs = String.format("%02d", progHrs);
        String sPosMins = String.format("%02d", progMins);
        String sPosSecs = String.format("%02d", progSecs);
        String progFull = sPosHrs + ":" + sPosMins + ":" + sPosSecs;
        if (sPosHrs.equals("00")) {
            progFull = sPosMins + ":" + sPosSecs;
        }
        tvProgress.setText(progFull);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        changingPos = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        changingPos = false;
        seekTo(prog);
        setViews();
    }
    // endregion

    // region onMethods
    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            paused = false;
        }
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ExitApp();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!isLib) {
            if (isPlaying()) {
                moveTaskToBack(true);
            } else {
                ExitApp();
            }
        } else {
            SwitchViews();
        }
    }
    // endregion

    // region SwitchView
    void SwitchViews() {
        if (!isLib) {
            viewSwitcher.setDisplayedChild(1);
            setViews();
            isLib = true;
        } else {
            viewSwitcher.setDisplayedChild(0);
            setViews();
            isLib = false;
        }
    }

    // endregion

    // region GetLists
    // region AlbumArt
    Bitmap getAlbumart(Long album_id) {
        Bitmap bm = null;
        try {
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
            Log.e("GET ALBUM ART", "Error getting album art", e);
        }
        return bm;
    }
    // endregion

    void getGenreList() {
        int index;
        long genreId;
        Uri uri;
        Cursor genrecursor;
        Cursor tempcursor;
        String[] proj1 = {MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID};
        String[] proj2 = {MediaStore.Audio.Media.DISPLAY_NAME};

        genrecursor = this.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, proj1, null, null, null);
        if (genrecursor.moveToFirst()) {
            do {
                index = genrecursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
                Log.i("Tag-Genre name", genrecursor.getString(index));

                index = genrecursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
                genreId = Long.parseLong(genrecursor.getString(index));
                uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);

                int idColumn = genrecursor.getColumnIndex(MediaStore.Audio.Media._ID);

                tempcursor = this.getContentResolver().query(uri, proj2, null, null, null);
                Log.i("Tag-Number of songs for this genre", tempcursor.getCount() + "");
                if (tempcursor.moveToFirst()) {
                    do {
                        index = tempcursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                        Log.i("Tag-Song name", tempcursor.getString(index));

                        long thisId = genrecursor.getLong(idColumn);
                        String thisGenre = genrecursor.getString(index);

                        genreList.add(new Genre(thisId, thisGenre));
                    } while (tempcursor.moveToNext());
                }
            } while (genrecursor.moveToNext());
        }
/*
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
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
*/
    }

    void getArtistList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ARTIST);
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

    void getAlbumList() {
        //retrieve album info
        ContentResolver musicResolver = getContentResolver();
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

    void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int artColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisArt = musicCursor.getString(artColumn);

                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, thisArt));
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }
    // endregion

    // region MusicStuff
// region ServiceConnection
    private final ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    // endregion

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    // region GettersCans
    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    // endregion

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getDur();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        } else {
            return 0;
        }
    }

    void PlayPause() {
        if (!musicSrv.isPng()) {
            start();
        } else {
            pause();
        }
        setViews();
    }

    @Override
    public void pause() {
        if (musicSrv != null && musicBound) {
            paused = true;
            musicSrv.pausePlayer();
            setViews();
        }
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        if (musicSrv != null && musicBound) {
            paused = false;
            musicSrv.go();
            setViews();
        }
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            paused = false;
            return true;
        } else {
            paused = true;
            return false;
        }
    }

    void playNext() {
        musicSrv.playNext();
        setViews();
    }

    void playPrev() {
        musicSrv.playPrev();
        setViews();
    }

    void SetShuffle() {
        if (!musicSrv.shuffle) {
            musicSrv.repeatone = false;
            musicSrv.repeatall = false;
            ibRepeat.setImageResource(R.drawable.ic_av_repeat);
            musicSrv.shuffle = true;
            ibShuffle.setImageResource(R.drawable.ic_av_shuffle_on);
        } else {
            musicSrv.shuffle = false;
            ibShuffle.setImageResource(R.drawable.ic_av_shuffle);
        }

    }

    void SetRepeat() {
        if (!musicSrv.repeatall) {
            musicSrv.shuffle = false;
            ibShuffle.setImageResource(R.drawable.ic_av_shuffle);
            musicSrv.repeatall = true;
            musicSrv.repeatone = false;
            ibRepeat.setImageResource(R.drawable.ic_av_repeat_all);
        }
        if (!musicSrv.repeatone) {
            musicSrv.shuffle = false;
            ibShuffle.setImageResource(R.drawable.ic_av_shuffle);
            musicSrv.repeatall = false;
            musicSrv.repeatone = true;
            ibRepeat.setImageResource(R.drawable.ic_av_repeat_one);
        } else {
            musicSrv.repeatall = false;
            musicSrv.repeatone = false;
            ibRepeat.setImageResource(R.drawable.ic_av_repeat);
        }
    }
    // endregion

    // region onPicked
/*
    public void genrePicked(View view) {
        setViews();
    }

    public void albumPicked(View view) {
        setViews();
    }
*/

    public void artistPicked(View v) {
        artistsFragment.artistPicked(v);
        setViews();
    }

    public void songPicked(View view) {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        setViews();
    }
    // endregion

    // region setViews
    private void setViews() {
        if (musicSrv != null && musicBound && loaded) {
            Song getSong = songList.get(musicSrv.songPosn);
            String songTitle = getSong.getTitle();
            String songArtist = getSong.getArtist();
            String songAlbum = getSong.getAlbum();
            String songArt = getSong.getArt();

            tvTitle.setText(songTitle);
            tvArtist.setText(songArtist);
            tvAlbum.setText(songAlbum);
            tvLibTitle.setText(songTitle);
            tvLibArtist.setText(songArtist);
            tvLibAlbum.setText(songAlbum);

            if (isPlaying()) {
                ibPlay.setImageResource(R.drawable.ic_av_pause);
                ibLibPlay.setImageResource(R.drawable.ic_av_pause);
            } else {
                ibPlay.setImageResource(R.drawable.ic_av_play_arrow);
                ibLibPlay.setImageResource(R.drawable.ic_av_play_arrow);
            }

            // region DurationPosition
            int dur = musicSrv.songDuration;
            int pos = getCurrentPosition();

            int durHrs = (int) TimeUnit.MILLISECONDS.toHours(dur) % 24;
            int durMins = (int) TimeUnit.MILLISECONDS.toMinutes(dur) % 60;
            int durSecs = (int) TimeUnit.MILLISECONDS.toSeconds(dur) % 60;
            String sDurHrs = String.format("%02d", durHrs);
            String sDurMins = String.format("%02d", durMins);
            String sDurSecs = String.format("%02d", durSecs);
            String duration = sDurHrs + ":" + sDurMins + ":" + sDurSecs;
            if (sDurHrs.equals("00")) {
                duration = sDurMins + ":" + sDurSecs;
            }

            int posHrs = (int) TimeUnit.MILLISECONDS.toHours(pos) % 24;
            int posMins = (int) TimeUnit.MILLISECONDS.toMinutes(pos) % 60;
            int posSecs = (int) TimeUnit.MILLISECONDS.toSeconds(pos) % 60;
            String sPosHrs = String.format("%02d", posHrs);
            String sPosMins = String.format("%02d", posMins);
            String sPosSecs = String.format("%02d", posSecs);
            String position = sPosHrs + ":" + sPosMins + ":" + sPosSecs;
            if (sPosHrs.equals("00")) {
                position = sPosMins + ":" + sPosSecs;
            }

            sbProgress.setMax(dur);

            tvTotal.setText(duration);

            if (!paused) {
                if (!changingPos) {
                    sbProgress.setProgress(pos);
                }
                tvProgress.setText(position);
            }

            // endregion

            Long albumId = Long.valueOf(songArt);
            Bitmap img = getAlbumart(albumId);
            ivArt3.setImageBitmap(img);
            ivLibArt.setImageBitmap(img);
            if (ivArt3.getDrawable() == null) {
                ivArt3.setImageResource(R.drawable.ic_albumart);
                ivLibArt.setImageResource(R.drawable.ic_albumart);
            }

        }
    }
    // endregion

    // region Tests
    void Tests() {
        handler = new Handler();
        run = new Runnable() {
            @Override
            public void run() {
                if (musicSrv != null && musicBound && !loaded) {
                    int defSong = sharedPreferences.getInt(keySong, 0);
                    //int defPos = sharedPreferences.getInt(keyPos, 0);
                    musicSrv.setSong(defSong);
                    //seekTo(defPos);
                    musicSrv.playSong();
                    pause();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                        playNext();
                    }
                    loaded = true;
                }
                setViews();
                handler.postDelayed(this, runSpeed);
            }
        };
        handler.postDelayed(run, runSpeed);
    }
    // endregion

    // region Exit
    void ExitApp() {
        if (musicSrv != null && musicBound) {
            int songInt;
            if (musicSrv.songPosn >= 0) {
                songInt = musicSrv.songPosn - 1;
            } else {
                songInt = 0;
            }
            //int posInt = getCurrentPosition();
            sharedPreferences.edit()
                    .putInt(keySong, songInt)
                            //.putInt(keyPos, pos)
                    .apply();

            stopService(playIntent);
            unbindService(musicConnection);
            musicSrv = null;
            finish();
            //System.exit(0);
        }
    }

    public ArtistsFragment getArtistsFragment() {
        return artistsFragment;
    }

    public void setArtistsFragment(ArtistsFragment artistsFragment) {
        this.artistsFragment = artistsFragment;
    }
    // endregion
}