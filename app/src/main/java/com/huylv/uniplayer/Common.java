package com.huylv.uniplayer;

import android.app.Application;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;

import com.huylv.uniplayer.service.MusicService;

/**
 * Created by HuyLV-CT on 24-Aug-16.
 */
public class Common extends Application {

    public static final String UPDATE_UI_BROADCAST = "com.huylv.uniplayer.NEW_SONG_UPDATE_UI";
    //Update UI broadcast flags.
    public static final String UPDATE_SEEKBAR_DURATION = "UpdateSeekbarDuration";
    public static final String UPDATE_PAGER_POSTIION = "UpdatePagerPosition";
    public static final String UPDATE_PLAYBACK_CONTROLS = "UpdatePlabackControls";
    public static final String SERVICE_STOPPING = "ServiceStopping";
    public static final String SHOW_STREAMING_BAR = "ShowStreamingBar";
    public static final String HIDE_STREAMING_BAR = "HideStreamingBar";
    public static final String UPDATE_BUFFERING_PROGRESS = "UpdateBufferingProgress";
    public static final String INIT_PAGER = "InitPager";
    public static final String NEW_QUEUE_ORDER = "NewQueueOrder";
    public static final String UPDATE_EQ_FRAGMENT = "UpdateEQFragment";
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
