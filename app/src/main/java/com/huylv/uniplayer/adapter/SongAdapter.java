package com.huylv.uniplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.huylv.uniplayer.MainActivity;
import com.huylv.uniplayer.R;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.service.MusicService;
import com.huylv.uniplayer.task.Config;

import java.util.ArrayList;

/**
 * Created by HuyLV-CT on 18-Aug-16.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private static final long FADE_DURATION = 1000;
    ArrayList<Song> songArrayList;
    Context context;

    public SongAdapter(ArrayList<Song> songArrayList, Context context) {
        this.songArrayList = songArrayList;
        this.context = context;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_list, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, final int position) {
        Song thisSong = songArrayList.get(position);
        holder.title.setText(thisSong.getName());
        holder.description.setText(thisSong.getArtist());
        holder.song_item_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).setSelectedSong(position, MusicService.NOTIFICATION_ID);
            }
        });

        if(thisSong.getBitmapString()!=null) {
            Bitmap bitmap = Config.StringToBitMap(thisSong.getBitmapString());
            holder.playing.setImageBitmap(bitmap);
        }
        else Log.e("cxz",thisSong.getName()+" null image");
        setFadeAnimation(holder.itemView);
    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

    class SongViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView description;
        ImageView playing;
        CardView song_item_cv;

        SongViewHolder(View v){
            super(v);
            title = (TextView)v.findViewById(R.id.item_song_title);
            description = (TextView)v.findViewById(R.id.item_song_description);
            playing = (ImageView)v.findViewById(R.id.item_song_iv);
            song_item_cv = (CardView)v.findViewById(R.id.item_song_cv);
        }
    }
}
