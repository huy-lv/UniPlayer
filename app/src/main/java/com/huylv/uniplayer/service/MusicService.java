package com.huylv.uniplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
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

import com.huylv.uniplayer.R;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.task.Config;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer mPlayer;
    private Uri mSongUri;

    private ArrayList<Song> mListSongs;
    private int SONG_POS = 0;

    private final IBinder musicBind = new PlayerBinder();

    private final String ACTION_STOP = "com.uniplayer.STOP";
    private final String ACTION_NEXT = "com.uniplayer.NEXT";
    private final String ACTION_PREVIOUS = "com.uniplayer.PREVIOUS";
    private final String ACTION_PAUSE = "com.uniplayer.PAUSE";

    private static final int STATE_PAUSED = 1;
    private static final int STATE_PLAYING = 2;
    private int mState = 0;
    private static final int REQUEST_CODE_PAUSE = 101;
    private static final int REQUEST_CODE_PREVIOUS = 102;
    private static final int REQUEST_CODE_NEXT = 103;
    private static final int REQUEST_CODE_STOP = 104;
    public static int NOTIFICATION_ID = 11;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationCompat mNotification;
    private RemoteViews notificationView;
    private RemoteViews expNotificationView;

    public class PlayerBinder extends Binder {//Service connection to play in background

        public MusicService getService() {
            Log.d("test", "getService()");
            return MusicService.this;
        }
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
        mPlayer = new MediaPlayer();
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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Stop the Mediaplayer
        mPlayer.stop();
        mPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.reset();
        try {
            if (SONG_POS != mListSongs.size() - 1) {
                SONG_POS++;
            } else
                SONG_POS = 0;
            mPlayer.setDataSource(getApplicationContext(), mListSongs.get(SONG_POS).getSongUri());
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mPlayer.prepareAsync();

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
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
        updateNotification(song);
    }

    public void playPauseSong() {

        if (mState == STATE_PAUSED) {
            notificationView.setImageViewResource(R.id.notification_expanded_base_play, android.R.drawable.ic_media_pause);
            mState = STATE_PLAYING;
            mPlayer.start();

        } else {
            notificationView.setImageViewResource(R.id.notification_expanded_base_play, android.R.drawable.ic_media_play);
            mState = STATE_PAUSED;
            mPlayer.pause();
        }
        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        manager.updateAppWidget(NOTIFICATION_ID, notificationView);
    }

    public void stopSong() {
        mPlayer.stop();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    public void nextSong() {
        startSong(mListSongs.get(SONG_POS + 1));
        SONG_POS++;
    }

    public void previousSong() {
        startSong(mListSongs.get(SONG_POS - 1));
        SONG_POS--;
    }

    public void setSongURI(Uri uri) {
        this.mSongUri = uri;
    }



    /*public void setSongList(ArrayList<Song> listSong, int pos, int notification_id) {
        mListSongs = listSong;
        SONG_POS = pos;
        NOTIFICATION_ID = notification_id;
    }*/

    public void setSelectedSong(int pos, int notification_id) {
        SONG_POS = pos;
        NOTIFICATION_ID = notification_id;
        setSongURI(mListSongs.get(SONG_POS).getSongUri());
        showNotification();
        startSong(mListSongs.get(SONG_POS));
    }

    public void setSongList(ArrayList<Song> listSong) {
        mListSongs = listSong;
    }

    public void showNotification() {
        PendingIntent pendingIntent;
        Intent intent;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationView = new RemoteViews(getPackageName(), R.layout.notification_custom_layout);
        expNotificationView = new RemoteViews(getPackageName(),R.layout.notification_custom_expanded_layout);

        notificationView.setTextViewText(R.id.notification_expanded_base_line_one, mListSongs.get(SONG_POS).getName());

        intent = new Intent(ACTION_STOP);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_STOP, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, pendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, pendingIntent);

        intent = new Intent(ACTION_PAUSE);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PAUSE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

        notificationBuilder.setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setCustomContentView(notificationView)
                .setCustomBigContentView(expNotificationView)
                .setDefaults(Notification.FLAG_NO_CLEAR);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotification(Song song) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationView.setTextViewText(R.id.notification_expanded_base_line_one, song.getName());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_one, song.getName());

        notificationView.setTextViewText(R.id.notification_expanded_base_line_two, song.getArtist());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_two, song.getArtist());

        notificationView.setImageViewBitmap(R.id.notification_base_image, Config.StringToBitMap(song.getBitmapString()));
        expNotificationView.setImageViewBitmap(R.id.notification_base_image, Config.StringToBitMap(song.getBitmapString()));
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
