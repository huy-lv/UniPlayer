package com.huylv.uniplayer.task;

import android.content.Context;
import android.os.AsyncTask;

import com.huylv.uniplayer.model.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

/**
 * Created by HuyLV-CT on 27-Aug-16.
 */
public class Download1MusicTask extends AsyncTask<Song, Void, String> {
    Context context;
    File rootFolder;

    public Download1MusicTask(Context c) {
        context = c;
    }

    @Override
    protected String doInBackground(Song... songs) {
        rootFolder = Config.rootFolder;
        String downloadLink = getDownloadLink(songs[0].getDownloadLink());
        return downloadLink;
    }

    private String getDownloadLink(String songLink) {
        String link = null;
        try {
            songLink = songLink.substring(0, songLink.length() - 5);
            Document docSong = Jsoup.connect(Config.CSN_URL + songLink + "_download.html").get();
            String cssDownloadLink = "#downloadlink > b";
            Element e = docSong.select(cssDownloadLink).first();
//            Log.e("cxz",Config.CSN_URL+songLink+"_download.html"+"\n e:"+docSong.html());
            String tempHtml = e.html();
            int t1 = tempHtml.indexOf("document.write");
            int t2 = tempHtml.indexOf(".mp3");
            link = tempHtml.substring(t1 + 124, t2 + 4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return link;
    }
}
