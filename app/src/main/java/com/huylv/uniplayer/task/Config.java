package com.huylv.uniplayer.task;

import com.google.firebase.database.FirebaseDatabase;
import com.huylv.uniplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by HuyLV-CT on 18-Aug-16.
 */
public class Config {
    public static final String Firebase_Url = "https://uniplayer-849b7.firebaseio.com/";
    public static final String CSN_URL = "http://chiasenhac.vn/";
    public static ArrayList<Song> localSongList = new ArrayList<>();
    public static ArrayList<Song> serverSongList = new ArrayList<>();
    public static ArrayList<Song> songToDownload = new ArrayList<>();
    public static ArrayList<Song> songToDelete = new ArrayList<>();
    public static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();;
    public static Song playingSong;
}
