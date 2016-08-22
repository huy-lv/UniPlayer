package com.huylv.uniplayer.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.huylv.uniplayer.MainActivity;
import com.huylv.uniplayer.model.Song;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import io.realm.Realm;

/**
 * Created by HuyLV-CT on 16-Aug-16.
 */
public class BuildLibraryTask  extends AsyncTask<Void,Void,Integer> {

    Context context;

    public BuildLibraryTask(Context c){
        context = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Integer doInBackground(Void... params) {

        //create root directory
        File rootFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "music");
        boolean success = true;
        if(!rootFolder.exists()){
            success = rootFolder.mkdir();
        }else{
            Log.e("cxz", "folder exist");
        }
        if(success){
            Log.e("cxz","root folder created");
        }else{
            Log.e("cxz","create error");
            return -4;
        }

        Config.songToDownload.clear();
        Config.songToDelete.clear();
        Config.localSongList.clear();

        Realm realm = Realm.getDefaultInstance();

        //get list music
        File[] listFile = rootFolder.listFiles();
        realm.beginTransaction();
        realm.deleteAll();
        for(File f: listFile){

            MediaMetadataRetriever mdr = new MediaMetadataRetriever();
            mdr.setDataSource(rootFolder+"/"+f.getName());
            int length = Integer.parseInt(mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000;
            String artist = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if(artist==null || artist.equals("")) artist = "Unknown";
            String name = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if(name==null) name = f.getName();
            //picture
            InputStream inputStream = null;
            if(mdr.getEmbeddedPicture()!=null){
                inputStream = new ByteArrayInputStream(mdr.getEmbeddedPicture());
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            mdr.release();
            Song song = new Song(name,artist,f.getAbsolutePath(),length,Config.BitMapToString(bitmap));
            Config.localSongList.add(song);
            realm.copyToRealm(song);
        }
        realm.commitTransaction();


        //check list online
        syncWithLocal();
        return null;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        Log.e("cxz","build done");
        if(Config.songToDownload.size()>0 || Config.songToDelete.size()>0) {
            DownloadMusicTask downloadMusicTask = new DownloadMusicTask(context);
            downloadMusicTask.execute();
            Toast.makeText(context, "not synced, download:"+Config.songToDownload.size()+" delete:"+Config.songToDelete.size(), Toast.LENGTH_SHORT).show();
        } else {
            ((MainActivity)context).stopSyncAnim();
            ((MainActivity)context).refreshSongList();
            Toast.makeText(context, "Everything synced! "+Config.localSongList.size()+" in local", Toast.LENGTH_SHORT).show();
        }
    }

    private void syncWithLocal() {
        //check each server song
        for(Song s : Config.serverSongList){
            int temp = getSongExistsInLocal(s);
            if(temp == -1){
                //song not in local
                Config.songToDownload.add(s);
            }else{
                //song in local
                Config.localSongList.get(temp).setAvailableOnServer(true);
            }
        }

        //add to delete list
        for(Song s:Config.localSongList){
            if(!s.isAvailableOnServer()){
                Config.songToDelete.add(s);
            }
        }
    }

    private int getSongExistsInLocal(Song serverSong){
        for(int i=0;i<Config.localSongList.size();i++){
            if(serverSong.equals(Config.localSongList.get(i))) return i;
        }
        return -1;
    }


}
