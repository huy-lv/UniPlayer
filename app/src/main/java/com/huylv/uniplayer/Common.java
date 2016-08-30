package com.huylv.uniplayer;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.huylv.uniplayer.service.MusicService;

/**
 * Created by HuyLV-CT on 24-Aug-16.
 */
public class Common extends Application {

    public static final String UPDATE_UI_BROADCAST = "com.huylv.uniplayer.NEW_SONG_UPDATE_UI";
    //Update UI broadcast flags.
    public static final String UPDATE_UI_SONG_AND_PLAYBACK = "UpdatePagerPosition";
    public static final String SERVICE_STOPPING = "ServiceStopping";
    public static final String KEY_FIRST_RUN = "KEY_FIRST_RUN";
    public static final String START_SONG_POS = "START_SONG_POS";
    private LocalBroadcastManager mLocalBroadcastManager;
    private boolean mIsServiceRunning = false;
    private MusicService mService;
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean isServiceRunning() {
        return mIsServiceRunning;
    }

    public void setIsServiceRunning(boolean running) {
        mIsServiceRunning = running;
    }

    public MusicService getService() {
        return mService;
    }

    public void setService(MusicService service) {
        mService = service;
    }

    public boolean isPlayingMusic() {
        try {
            return getCurrentMediaPlayer().isPlaying();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public MediaPlayer getCurrentMediaPlayer() {
        return mMediaPlayer;
    }

    public void broadcastUpdateUICommand(String[] updateFlags, String[] flagValues) {
        Intent intent = new Intent(UPDATE_UI_BROADCAST);
        for (int i = 0; i < updateFlags.length; i++) {
            intent.putExtra(updateFlags[i], flagValues[i]);
        }

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.sendBroadcast(intent);


    }

    public void saveFirstRunToSp() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_FIRST_RUN, false);
        editor.apply();
    }

    public boolean readFirstRun() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(KEY_FIRST_RUN, true);
    }

//    public boolean isServiceRunning(Class<?> serviceClass) {
//        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
//
//        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
//            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
//                return true;
//            }
//        }
//        return false;
//    }
}
