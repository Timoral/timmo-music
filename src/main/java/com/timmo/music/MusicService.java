package com.timmo.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    // region GlobalVars
    NotificationManager notificationManager;

    private MediaPlayer player;
    private ArrayList<Song> songs;
    public int songPosn = 0;
    public int songDuration = 0;

    private int drawablePlayPause = R.drawable.ic_stat_av_pause;
    private int drawablePlayPauseNotify = R.drawable.ic_stat_av_pause;
    private String sPlayPause = "Pause";
    private boolean ongoing = true;

    private final IBinder musicBind = new MusicBinder();

    private String songTitle = "";
    private String songArtist = "";
    private String songAlbum = "";
    private String songArt = "";
    private static final int NOTIFY_ID = 1;

    public boolean shuffle = false;
    public boolean repeatone = false;
    public boolean repeatall = false;
    private Random rand;

    private boolean bFirstPrepare = false;

    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    //public static final String CMDNAME = "command";
    private static final String CMDTOGGLEPAUSE = "togglepause";
    private static final String CMDSTOP = "stop";
    private static final String CMDPAUSE = "pause";
    private static final String CMDPREVIOUS = "previous";
    private static final String CMDNEXT = "next";
    private static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
    private static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
    private static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
    private static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
    // endregion

    // region IntentReceiver
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            ///MusicUtils.debugLog("mIntentReceiver.onReceive " + action + " / " + cmd);
            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                //next(true);
                playNext();
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
                playPrev();
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
                if (player.isPlaying()) {
                    //mPausedByTransientLossOfFocus = false;
                    pausePlayer();
                } else {
                    go();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                //mPausedByTransientLossOfFocus = false;
                pausePlayer();
            } else if (CMDSTOP.equals(cmd)) {
                //mPausedByTransientLossOfFocus = false;
                pausePlayer();
                seek(0);
            } /*else if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
                // Someone asked us to refresh a set of specific widgets, probably
                // because they were just added.
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetProvider.performUpdate(MusicService.this, appWidgetIds);
            }*/
        }
    };
    // endregion

    // region Init
    public void onCreate() {
        //create the service
        //create the service
        super.onCreate();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        //noinspection StatementWithEmptyBody
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //could not get audio focus.
        }

        //initialize position
        songPosn = 0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
        rand = new Random();
        bFirstPrepare = true;
        //MusicActivity musicActivity = new MusicActivity();
    }

    void initMusicPlayer() {
        //set player properties
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }
    // endregion

    // region PreparePlayer
    @Override
    public void onPrepared(MediaPlayer mp) {
        songDuration = mp.getDuration();
        //start playback
        go();

        if (bFirstPrepare) {
            pausePlayer();
            bFirstPrepare = false;
        }

        InitNotification();
    }
    // endregion

    // region Notification
    void InitNotification() {
        // region Intents
        Intent openIntent = new Intent(this, MusicActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(SERVICECMD);
        commandFilter.addAction(TOGGLEPAUSE_ACTION);
        commandFilter.addAction(NEXT_ACTION);
        commandFilter.addAction(PREVIOUS_ACTION);
        registerReceiver(mIntentReceiver, commandFilter);

        Intent iPrevious = new Intent();
        iPrevious.setAction(PREVIOUS_ACTION);
        Intent iPlayPause = new Intent();
        iPlayPause.setAction(TOGGLEPAUSE_ACTION);
        Intent iNext = new Intent();
        iNext.setAction(NEXT_ACTION);

        PendingIntent piPrevious = PendingIntent.getBroadcast(this, 100, iPrevious, 0);
        PendingIntent piPlayPause = PendingIntent.getBroadcast(this, 100, iPlayPause, 0);
        PendingIntent piNext = PendingIntent.getBroadcast(this, 100, iNext, 0);
        // endregion

        Long albumId = Long.valueOf(songArt);
        Bitmap art = getAlbumart(albumId);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notify;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final MediaSession mediaSession = new MediaSession(this, "debug tag");
            mediaSession.setMetadata(new MediaMetadata.Builder()
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, art)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artist")
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, "Album")
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Title")
                    .build());
            // Indicate you're ready to receive media commands
            mediaSession.setActive(true);
            // Attach a new Callback to receive MediaSession updates
/*
            mediaSession.setCallback(new MediaSession.Callback() {

                // Implement your callbacks

            });
*/
            // Indicate you want to receive transport controls via your Callback
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

            notify = new Notification.Builder(this)
                    .setContentIntent(pendInt)
                    .setShowWhen(false)
                    .setStyle(new Notification.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(1, 2))
                    .setColor(getResources().getColor(R.color.accent))
                    .setLargeIcon(art)
                    .setSmallIcon(drawablePlayPauseNotify)
                    .setOngoing(ongoing)
                    .setContentText(songArtist)
                    .setContentInfo(songAlbum)
                    .setContentTitle(songTitle)
                    .addAction(R.drawable.ic_stat_av_skip_previous, "Previous", piPrevious)
                    .addAction(drawablePlayPause, sPlayPause, piPlayPause)
                    .addAction(R.drawable.ic_stat_av_skip_next, "Next", piNext)
                    .build();
            //startForeground(NOTIFY_ID, notify);

            notificationManager.notify(1, notify);
            //((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, notify);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentIntent(pendInt)
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setSmallIcon(drawablePlayPauseNotify)
                    .setLargeIcon(art)
                    .setColor(getResources().getColor(R.color.accent))
                    .setTicker(songTitle)
                    .setOngoing(ongoing)
                    .setContentTitle(songTitle)
                    .setContentText(songArtist + " - " + songAlbum)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(songArtist + "\n" + songAlbum)
                            .setBigContentTitle(songTitle))
                    .addAction(R.drawable.ic_stat_av_skip_previous, "Previous", piPrevious)
                    .addAction(drawablePlayPause, sPlayPause, piPlayPause)
                    .addAction(R.drawable.ic_stat_av_skip_next, "Next", piNext);
            notify = builder.build();

            notificationManager.notify(NOTIFY_ID, notify);
            //startForeground(NOTIFY_ID, notify);
        }
    }
    // endregion

    // region onAudioFocusChange
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (player == null) {
                    initMusicPlayer();
                } else if (!player.isPlaying()) {
                    player.start();
                }
                player.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
/*
                if (player.isPlaying()) {
                    player.stop();
                }
*/
                player.release();
                player = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()) player.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
        }
    }
    // endregion

    // region ServiceBinding

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    // endregion

    // region AlbumArt
    Bitmap getAlbumart(Long album_id) {
        Bitmap bm = null;
        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = this.getContentResolver()
                    .openFileDescriptor(uri, "r");

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

    // region GettersSettersAndTesters
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            if (!repeatone || !repeatall) {
                mp.reset();
                playNext();
            } else if (repeatall && songPosn >= songs.size()) {
                songPosn = 0;
                playSong();
            } else {
                playSong();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public void playSong() {
        //play a song
        player.reset();
        //get song
        if (songPosn < 0) {
            songPosn = 0;
        }
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        songAlbum = playSong.getAlbum();
        songArt = playSong.getArt();

        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }
    //endregion

    // region MusicCommands
    public void pausePlayer() {
        player.pause();
        drawablePlayPause = R.drawable.ic_stat_av_play_arrow;
        drawablePlayPauseNotify = R.drawable.ic_stat_av_pause;
        sPlayPause = "Play";
        ongoing = false;
        InitNotification();
    }

    public void go() {
        player.start();
        drawablePlayPause = R.drawable.ic_stat_av_pause;
        drawablePlayPauseNotify = R.drawable.ic_stat_av_play_arrow;
        sPlayPause = "Pause";
        ongoing = true;
        InitNotification();
    }

    public void playPrev() {
        if (getPosn() < 2000) {
            songPosn--;
        }
        if (songPosn < 0) {
            songPosn = songs.size() - 1;
        }
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        } else {
            songPosn++;
            if (songPosn >= songs.size()) {
                songPosn = 0;
            }
        }
        playSong();
    }

    public void seek(int posn) {
        player.seekTo(posn);
        player.start();
    }
    // endregion

    // region onDestroy
    @Override
    public void onDestroy() {
        unregisterReceiver(mIntentReceiver);
        //stopForeground(true);
        notificationManager.cancel(NOTIFY_ID);
    }
    // endregion
}