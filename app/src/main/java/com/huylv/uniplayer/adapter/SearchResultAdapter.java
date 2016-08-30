package com.huylv.uniplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.huylv.uniplayer.R;
import com.huylv.uniplayer.animation.ImageViewWithAnim;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.model.SongFireBase;
import com.huylv.uniplayer.task.Config;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.util.ArrayList;

/**
 * Created by HuyLV-CT on 26-Aug-16.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    Context c;
    ArrayList<Song> results;
    private DatabaseReference songTable;

    public SearchResultAdapter(Context context, ArrayList<Song> results) {
        c = context;
        this.results = results;
    }

    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SearchResultViewHolder holder, final int position) {
        final Song s = results.get(position);
        holder.search_result_song_name.setText(s.getName());
        holder.search_result_song_artist.setText(s.getArtist());
        holder.search_result_song_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Download1MusicTask d1 = new Download1MusicTask(c);
//                d1.execute(results.get(position));
//                try {
//                    String r = d1.get();
//                    if(r != null)   startDownload(s.getName(),s.getArtist(),r,holder);
//                    else Log.e("Cxz","download link null");
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
                addSongToList(s);
            }
        });
        holder.search_result_song_type.setImageDrawable(ContextCompat.getDrawable(c, s.getLength() == 1 ? R.drawable.ic_music_video_black_24dp : R.drawable.ic_movie_black_24dp));
    }

    private void addSongToList(Song s) {
        songTable = Config.firebaseDatabase.getReference("Song");
        songTable.child("00" + (Config.serverSongList.size() + 1)).setValue(new SongFireBase(s.getName(), s.getArtist()));
    }

    private void startDownload(String name, String a, String downloadLink, final SearchResultViewHolder holder) {
        ThinDownloadManager tdm = new ThinDownloadManager();

        Uri downloadUri = Uri.parse(downloadLink);
        Uri destinationUri = Uri.parse(Config.rootFolder + "/" + name + "-" + a + ".mp3");
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        Toast.makeText(c, "Download song done", Toast.LENGTH_SHORT).show();
                        holder.search_result_song_download.startAnimation(R.drawable.ic_done_black_24dp);
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        Toast.makeText(c, "Download failed:" + errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {

                    }
                });

        tdm.add(downloadRequest);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class SearchResultViewHolder extends RecyclerView.ViewHolder {
        TextView search_result_song_name;
        TextView search_result_song_artist;
        ImageViewWithAnim search_result_song_download;
        ImageView search_result_song_type;

        public SearchResultViewHolder(View itemView) {
            super(itemView);
            search_result_song_artist = (TextView) itemView.findViewById(R.id.search_result_song_artist);
            search_result_song_name = (TextView) itemView.findViewById(R.id.search_result_song_name);
            search_result_song_download = (ImageViewWithAnim) itemView.findViewById(R.id.search_result_song_download);
            search_result_song_type = (ImageView) itemView.findViewById(R.id.search_result_song_type);
        }
    }
}
