package com.timmo.music;

class Genre {
    private final long id;
    private final String genre;

    public Genre(long artistID, String thegenre) {
        id = artistID;
        genre = thegenre;
    }

    public long getID() {
        return id;
    }

    public String getGenre() {
        return genre;
    }

}
