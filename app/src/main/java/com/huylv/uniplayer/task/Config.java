package com.huylv.uniplayer.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.firebase.database.FirebaseDatabase;
import com.huylv.uniplayer.model.Song;

import java.io.ByteArrayOutputStream;
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

    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
