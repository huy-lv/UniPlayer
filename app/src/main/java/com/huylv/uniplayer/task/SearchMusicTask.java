package com.huylv.uniplayer.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.huylv.uniplayer.model.Song;

import org.apache.commons.httpclient.util.URIUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by HuyLV-CT on 26-Aug-16.
 */
public class SearchMusicTask extends AsyncTask<String, Void, ArrayList<Song>> {

    private Context context;
    private ArrayList<Song> songs;


    public SearchMusicTask(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<Song> doInBackground(String... string) {
        songs = new ArrayList<>();
        try {
            String s = URIUtil.encodeQuery(string[0]);
            Document doc = Jsoup.connect("http://search.chiasenhac.vn/search.php?s=" + s).get();
            int stt = 0;
            do {
                stt += 1;
                String cssPathNameSong = "body > div.mu-wrapper > div > div.m-left > div > div > div.pad > div.h-main > div.page-dsms > div.bod > table > tbody > tr:nth-child(" + (stt + 1) + ") > td:nth-child(2) > div > div > p:nth-child(1) > a";

                Element e = doc.select(cssPathNameSong).first();
                String name = e.text();
                String link = e.attr("href");

                String cssPathArtist = "body > div.mu-wrapper > div > div.m-left > div > div > div.pad > div.h-main > div.page-dsms > div.bod > table > tbody > tr:nth-child(" + (stt + 1) + ") > td:nth-child(2) > div > div > p:nth-child(2)";
                String artist = doc.select(cssPathArtist).first().text();

                String cssPathType = "body > div.mu-wrapper > div > div.m-left > div > div > div.pad > div.h-main > div.page-dsms > div.bod > table > tbody > tr:nth-child(" + (stt + 1) + ") > td:nth-child(3) > span > span";
                String type = doc.select(cssPathType).first().text();
                int intType = 0;
                if (type != null) {
                    if (type.equalsIgnoreCase("lossless") || type.contains("kbps")) {
                        intType = 1;//mean mp3
                    } else if (type.charAt(type.length() - 1) == 'p') {
                        intType = 2;//mean video
                    }

                    songs.add(new Song(name, artist, link, intType));
                } else {
                    Log.e("cxz", "type null");
                }


            } while (stt < 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return songs;
    }

}
