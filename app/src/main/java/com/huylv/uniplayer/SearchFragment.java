package com.huylv.uniplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.huylv.uniplayer.adapter.SearchResultAdapter;
import com.huylv.uniplayer.base.BaseFragment;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.task.SearchMusicTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by HuyLV-CT on 26-Aug-16.
 */
public class SearchFragment extends BaseFragment {

    @BindView(R.id.search_keyword_et)
    EditText search_keyword_et;
    @BindView(R.id.search_result)
    RecyclerView search_result;
    private SearchResultAdapter adapter;
    private ArrayList<Song> result;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        result = new ArrayList<>();
        adapter = new SearchResultAdapter(getActivity(), result);
        search_result.setLayoutManager(new LinearLayoutManager(getActivity()));
        search_result.setAdapter(adapter);
    }

    @OnClick(R.id.search_start_search_iv)
    void startSearch() {
        SearchMusicTask smt = new SearchMusicTask(getActivity());
        smt.execute(search_keyword_et.getText().toString());
        ArrayList<Song> songs = null;
        try {
            songs = smt.get();
            if (songs != null) {
                result.clear();
                result.addAll(songs);
                adapter.notifyDataSetChanged();
            } else {
                Log.e("cxz", "search result null");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }
}
