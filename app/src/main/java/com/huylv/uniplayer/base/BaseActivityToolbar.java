package com.huylv.uniplayer.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.huylv.uniplayer.R;

/**
 * Created by HuyLV-CT on 19-Aug-16.
 */
public abstract class BaseActivityToolbar extends BaseActivity {
    @Nullable
    protected Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toolBar = (Toolbar)findViewById(R.id.toolbar);
        if(toolBar == null){
            Log.e("cxz","null");
        }else {
            setSupportActionBar(toolBar);
        }
    }


}
