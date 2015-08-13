package com.timmo.music;

class Album {
    private final long id;
    private final String album;
    private final String art;

    public Album(long albumID, String thealbum, String albumArt) {
        id = albumID;
        album = thealbum;
        art = albumArt;

    }

    public long getID() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getArt() {
        return art;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;

        Album album1 = (Album) o;

        if (album != null ? !album.equals(album1.album) : album1.album != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return album != null ? album.hashCode() : 0;
    }

}
