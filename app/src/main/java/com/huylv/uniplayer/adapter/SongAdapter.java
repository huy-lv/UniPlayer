package com.huylv.uniplayer.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huylv.uniplayer.R;
import com.huylv.uniplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by HuyLV-CT on 18-Aug-16.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
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
    public void onBindViewHolder(SongViewHolder holder, int position) {
        holder.title.setText(songArrayList.get(position).getName());
        holder.description.setText(songArrayList.get(position).getArtist());
//        if(songArrayList.get(position).equals(Config.playingSong)){
//            holder.playing.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_media_pause));
//        }else{
            holder.playing.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_media_play));
//        }
        holder.song_item_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                context.getSupportMediaController().getTransportControls()
//                        .playFromMediaId(item.getMediaId(), null);
            }
        });
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
