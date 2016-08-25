package com.huylv.uniplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.huylv.uniplayer.Common;
import com.huylv.uniplayer.R;
import com.huylv.uniplayer.model.RepeatType;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.task.Config;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {


    private static final int STATE_PAUSED = 1;
    private static final int STATE_PLAYING = 2;
    private static final int REQUEST_CODE_PAUSE = 101;
    private static final int REQUEST_CODE_PREVIOUS = 102;
    private static final int REQUEST_CODE_NEXT = 103;
    private static final int REQUEST_CODE_STOP = 104;
    public static int NOTIFICATION_ID = 11;
    private final IBinder musicBind = new PlayerBinder();
    private final String ACTION_STOP = "com.uniplayer.STOP";
    private final String ACTION_NEXT = "com.uniplayer.NEXT";
    private final String ACTION_PREVIOUS = "com.uniplayer.PREVIOUS";
    private final String ACTION_PAUSE = "com.uniplayer.PAUSE";
    private RepeatType currentRepeatType = RepeatType.SHUFFLE;
    private MediaPlayer mPlayer;
    private Uri mSongUri;
    private ArrayList<Song> mListSongs;
    private int currentSongIndex = 0;
    private int mState = 0;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationCompat mNotification;
    private RemoteViews notificationView;
    private RemoteViews expNotificationView;
    private Common mApp;
    private boolean currentSong;
    private NotificationManager notificationManager;


    public MediaPlayer getMediaPlayer() {
        return mPlayer;
    }

    public RepeatType getCurrentRepeatType() {
        return currentRepeatType;
    }

    public void setCurrentRepeatType(RepeatType currentRepeatType) {
        this.currentRepeatType = currentRepeatType;
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("test", "onBind Called ");
        return musicBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Initializing the media player object
        mApp = (Common) getApplicationContext();
        initPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        notificationBuilder = new NotificationCompat.Builder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(ACTION_PAUSE)) {
                    playPauseSong();
                } else if (action.equals(ACTION_NEXT)) {
                    nextSong();
                } else if (action.equals(ACTION_PREVIOUS)) {
                    previousSong();
                } else if (action.equals(ACTION_STOP)) {
                    stopSong();
                    stopSelf();
                }
            }
        }
        mApp.setService(this);
        mApp.setIsServiceRunning(true);
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Stop the Mediaplayer
        mPlayer.stop();
        mPlayer.release();
        mApp.setIsServiceRunning(false);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.reset();
        try {
            incrementSongIndex();
            mPlayer.setDataSource(getApplicationContext(), mListSongs.get(currentSongIndex).getSongUri());
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mPlayer.prepareAsync();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApp.broadcastUpdateUICommand(new String[]{Common.SERVICE_STOPPING},
                new String[]{""});
        mApp.setService(null);
        mApp.setIsServiceRunning(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }

    private void initPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mPlayer = new MediaPlayer();

        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public boolean isPlayingMusic() {
        return mState == STATE_PLAYING;
    }

    public void startSong(Song song) {//Set data & start playing music
        mPlayer.reset();
        mState = STATE_PLAYING;
        mSongUri = song.getSongUri();
        try {
            mPlayer.setDataSource(getApplicationContext(), mSongUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mPlayer.prepareAsync();
        showNotification();
    }

    public void playPauseSong() {
//
        if (mState == STATE_PAUSED) {
            notificationView.setImageViewResource(R.id.notification_expanded_base_play, android.R.drawable.ic_media_pause);
            mState = STATE_PLAYING;
            mPlayer.start();

        } else {
            notificationView.setImageViewResource(R.id.notification_expanded_base_play, android.R.drawable.ic_media_play);
            mState = STATE_PAUSED;
            mPlayer.pause();
        }
        //Update the UI and scrobbler.
        String[] updateFlags = new String[]{Common.UPDATE_PLAYBACK_CONTROLS};
        String[] flagValues = new String[]{""};

        mApp.broadcastUpdateUICommand(updateFlags, flagValues);

        showNotification();
    }

    public void stopSong() {
        mPlayer.stop();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    void incrementSongIndex() {
        if (getCurrentRepeatType() == RepeatType.REPEAT_ALL) {
            if (currentSongIndex == mListSongs.size() - 1)
                currentSongIndex = 0;
            else {
                currentSongIndex++;
            }
        } else if (getCurrentRepeatType() == RepeatType.SHUFFLE) {
            //TODO
        }
    }

    public void nextSong() {
        incrementSongIndex();
        startSong(mListSongs.get(currentSongIndex));
    }

    public void previousSong() {
        startSong(mListSongs.get(currentSongIndex - 1));
        currentSongIndex--;
    }

    public void setSongURI(Uri uri) {
        this.mSongUri = uri;
    }

    public void setSelectedSong(int pos, int notification_id) {
        currentSongIndex = pos;
        NOTIFICATION_ID = notification_id;
        setSongURI(mListSongs.get(currentSongIndex).getSongUri());
        startSong(mListSongs.get(currentSongIndex));
    }



    /*public void setSongList(ArrayList<Song> listSong, int pos, int notification_id) {
        mListSongs = listSong;
        currentSongIndex = pos;
        NOTIFICATION_ID = notification_id;
    }*/

    public void setSongList(ArrayList<Song> listSong) {
        mListSongs = listSong;
    }

    public Notification buildNotification() {
        Song song = mListSongs.get(currentSongIndex);
        notificationView = new RemoteViews(getPackageName(), R.layout.notification_custom_layout);
        expNotificationView = new RemoteViews(getPackageName(), R.layout.notification_custom_expanded_layout);

        notificationView.setTextViewText(R.id.notification_expanded_base_line_one, mListSongs.get(currentSongIndex).getName());

        Intent intent = new Intent(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_STOP, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, pendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, pendingIntent);

        intent = new Intent(ACTION_PAUSE);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PAUSE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, playPauseTrackIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, pendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, pendingIntent);

        intent = new Intent(ACTION_PREVIOUS);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PREVIOUS, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, pendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, pendingIntent);

        intent = new Intent(ACTION_NEXT);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_NEXT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, pendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, pendingIntent);

        if (isPlayingMusic()) {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.ic_pause);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.ic_pause);
        } else {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.ic_play);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.ic_play);
        }

        notificationView.setTextViewText(R.id.notification_expanded_base_line_one, song.getName());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_one, song.getName());

        notificationView.setTextViewText(R.id.notification_expanded_base_line_two, song.getArtist());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_two, song.getArtist());

        notificationView.setImageViewBitmap(R.id.notification_base_image, Config.StringToBitMap(song.getBitmapString()));
        expNotificationView.setImageViewBitmap(R.id.notification_base_image, Config.StringToBitMap(song.getBitmapString()));

        notificationBuilder.setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setCustomContentView(notificationView)
                .setCustomBigContentView(expNotificationView)
                .setDefaults(Notification.FLAG_NO_CLEAR);
        return notificationBuilder.build();
    }

    public void showNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, buildNotification());
    }

    public class PlayerBinder extends Binder {//Service connection to play in background

        public MusicService getService() {
            Log.d("test", "getService()");
            return MusicService.this;
        }
    }
}
