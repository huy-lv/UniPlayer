package com.huylv.uniplayer.model;

/**
 * Created by HuyLV-CT on 27-Aug-16.
 */
public class SongFireBase {
    String name;
    String artist;

    public SongFireBase(String name, String artist) {

        this.name = name;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
