package com.huylv.uniplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.huylv.uniplayer.Common;
import com.huylv.uniplayer.MainActivity;

/**
 * Created by HuyLV-CT on 24-Aug-16.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    MainActivity mainActivity;

    public MyBroadcastReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //Grab the bundle from the intent.
        Bundle bundle = intent.getExtras();

        //update ui
        if (intent.hasExtra(Common.UPDATE_UI_SONG_AND_PLAYBACK)) {
            mainActivity.updateUI(Integer.parseInt(bundle.getString(Common.UPDATE_UI_SONG_AND_PLAYBACK)));
        }

        //Updates the playback control buttons.
//        if (intent.hasExtra(Common.UPDATE_PLAYBACK_CONTROLS)) {
//            mainActivity.setPlayPauseButton();
//            mainActivity.setRepeatButtonIcon();
//        }

        //Updates the duration of the SeekBar.
//        if (intent.hasExtra(Common.UPDATE_SEEKBAR_DURATION))
//            mainActivity.setSliderDuration(Integer.parseInt(
//                    bundle.getString(
//                            Common.UPDATE_SEEKBAR_DURATION)));


        //Close this activity if the service is about to stop running.
        if (intent.hasExtra(Common.SERVICE_STOPPING)) {
            mainActivity.mHandler.removeCallbacks(mainActivity.sliderUpdateRunnable);
            mainActivity.finish();
        }

    }
}
