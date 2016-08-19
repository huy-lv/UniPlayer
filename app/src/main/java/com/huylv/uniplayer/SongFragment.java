package com.huylv.uniplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.huylv.uniplayer.adapter.SongAdapter;
import com.huylv.uniplayer.base.BaseFragment;
import com.huylv.uniplayer.task.Config;

import butterknife.BindView;

/**
 * Created by HuyLV-CT on 19-Aug-16.
 */
public class SongFragment extends BaseFragment {
//public class SongFragment extends Fragment {

    @BindView(R.id.song_list_rv)
    RecyclerView song_list_rv;

    public SongAdapter adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SongAdapter(Config.localSongList,getActivity());
        song_list_rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        song_list_rv.setAdapter(adapter);
    }

    //    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_song,container,false);
//        song_list_rv = (RecyclerView)v.findViewById(R.id.song_list_rv);
//        adapter = new SongAdapter(Config.localSongList,getActivity());
//        song_list_rv.setLayoutManager(new LinearLayoutManager(getActivity()));
//        song_list_rv.setAdapter(adapter);
//        return v;
//    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_song;
    }
}
