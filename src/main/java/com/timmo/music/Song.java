package com.timmo.music;

class Song {
    private final long id;
    private final String title;
    private final String artist;
    private final String album;
    private final String art;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, String songArt) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        art = songArt;
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getArt() {
        return art;
    }
}
