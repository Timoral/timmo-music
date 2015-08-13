package com.timmo.music;

class Artist {
    private final long id;
    private final String artist;
    private final String art;

    public Artist(long artistID, String theartist, String artistArt) {
        id = artistID;
        artist = theartist;
        art = artistArt;
    }

    public long getID() {
        return id;
    }


    public String getArtist() {
        return artist;
    }

    public String getArt() {
        return art;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artist)) return false;

        Artist artist1 = (Artist) o;

        if (artist != null ? !artist.equals(artist1.artist) : artist1.artist != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return artist != null ? artist.hashCode() : 0;
    }
    
}
