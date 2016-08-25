package com.huylv.uniplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.huylv.uniplayer.animation.ImageViewWithAnim;
import com.huylv.uniplayer.base.BaseActivityDrawer;
import com.huylv.uniplayer.fragment.SongFragment;
import com.huylv.uniplayer.model.RepeatType;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.service.MusicService;
import com.huylv.uniplayer.service.MyBroadcastReceiver;
import com.huylv.uniplayer.task.BuildLibraryTask;
import com.huylv.uniplayer.task.Config;
import com.rey.material.widget.Slider;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class MainActivity extends BaseActivityDrawer {
    //public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final int MENU_SYNC = 12;
    public Handler mHandler = new Handler();
    SongFragment songFragment;
    @BindView(R.id.playing_song_rl)
    RelativeLayout playing_song_rl;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout sliding_layout;
    @BindView(R.id.playing_layout)
    RelativeLayout playing_layout;
    @BindView(R.id.panelToolbar)
    Toolbar panelToolbar;
    @BindView(R.id.playing_background)
    ImageView playing_background;
    @BindView(R.id.playing_song_name)
    TextView playing_song_name;
    @BindView(R.id.playing_song_artist)
    TextView playing_song_artist;
    @BindView(R.id.playing_song_thumbnail)
    ImageView playing_song_thumbnail;
    @BindView(R.id.playing_repeat)
    ImageViewWithAnim playing_repeat;
    @BindView(R.id.playing_play)
    ImageViewWithAnim playing_play;
    @BindView(R.id.playing_slider)
    Slider playing_slider;
    SlidingUpPanelLayout.PanelState currentState = SlidingUpPanelLayout.PanelState.COLLAPSED;
    private MenuItem refreshItem;
    private DatabaseReference songTable;
    private ChildEventListener addSongListener;
    private Intent playIntent;
    private boolean doubleBackToExitPressedOnce = false;
    private Common mApp;
    private AlphaAnimation mSeekbarStrobeAnim;
    private MyBroadcastReceiver mReceiver;
    private MusicService serviceMusic;
    public Runnable sliderUpdateRunnable = new Runnable() {

        public void run() {
            try {
                long currentPosition = serviceMusic.getMediaPlayer().getCurrentPosition();
                int currentPositionInSecs = (int) currentPosition / 1000;
                playing_slider.setValue(currentPositionInSecs, true);
                mHandler.postDelayed(sliderUpdateRunnable, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.PlayerBinder binder = (MusicService.PlayerBinder) service;
            //get service
            serviceMusic = binder.getService();
            serviceMusic.setSongList(Config.localSongList);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mApp = (Common) getApplicationContext();
        mReceiver = new MyBroadcastReceiver(this);

        createFireBase();
        songFragment = new SongFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_main, songFragment).addToBackStack(null).commit();

        initView();
    }

    private void initView() {
        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        panelToolbar.setBackgroundColor(Color.TRANSPARENT);
        panelToolbar.setVisibility(View.INVISIBLE);


        sliding_layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                panelToolbar.setVisibility(View.VISIBLE);
                playing_layout.setVisibility(View.VISIBLE);
                playing_layout.setAlpha(1 - slideOffset);
                panelToolbar.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case EXPANDED:
                        setPanelWhenExpanded();
                        break;
                    case COLLAPSED:
                        setPanelWhenCollapsed();
                        break;
                }
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        });

        playing_slider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                if (fromUser) {
                    mApp.getService().getMediaPlayer().seekTo(newValue * 1000);
                }
            }
        });
    }

    private void setPanelWhenCollapsed() {
        panelToolbar.setVisibility(View.INVISIBLE);
        playing_layout.setVisibility(View.VISIBLE);
        setSupportActionBar(toolBar);
        currentState = SlidingUpPanelLayout.PanelState.COLLAPSED;
    }

    private void setPanelWhenExpanded() {

        panelToolbar.setVisibility(View.VISIBLE);
        playing_layout.setVisibility(View.INVISIBLE);
        setSupportActionBar(panelToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentState = SlidingUpPanelLayout.PanelState.EXPANDED;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onBackPressed() {
        if (currentState == SlidingUpPanelLayout.PanelState.COLLAPSED || currentState == SlidingUpPanelLayout.PanelState.HIDDEN) {
            if (doubleBackToExitPressedOnce) {
                System.exit(0);
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }

        if (currentState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SYNC:
                if (Config.isOnline(this)) {
                    startSyncAnim();
                    (new BuildLibraryTask(MainActivity.this)).execute();
                } else {
                    Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        //////////////////////////add refresh button
        menu.add(Menu.NONE, MENU_SYNC, Menu.NONE, "Sync music")
                .setIcon(R.drawable.loading_icon)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setRefreshItem(menu.findItem(MENU_SYNC));
        return true;
    }

    protected void setRefreshItem(MenuItem item) {
        refreshItem = item;
    }

    public void startSyncAnim() {
        if (refreshItem != null) {
            refreshItem.setActionView(R.layout.indeterminate_progress_action);
        }
    }

    public void stopSyncAnim() {
        if (refreshItem != null) {
            refreshItem.setActionView(null);
        }
    }

    private void createFireBase() {
        //realm
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfig);

        if (!Config.isOnline(this)) {
            RealmQuery<Song> query = Realm.getDefaultInstance().where(Song.class);
            RealmResults<Song> results = query.findAll();
            Config.localSongList.clear();
            Config.localSongList.addAll(results);
        } else {
            songTable = Config.firebaseDatabase.getReference("Song");
            addSongListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Song ss = dataSnapshot.getValue(Song.class);
                    if (!checkIfSongExisted(ss)) {
                        Config.serverSongList.add(ss);
                        (new BuildLibraryTask(MainActivity.this)).execute();
                    }
                    Log.e("cxz", "child add:" + ss);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            songTable.addChildEventListener(addSongListener);
            songTable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (Config.serverSongList.size() > 0) {
//                        SyncMusicTask syncMusicTask = new SyncMusicTask(MainActivity.this);
//                        syncMusicTask.execute();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private boolean checkIfSongExisted(Song ss) {
        for (Song s : Config.serverSongList) {
            if (s.equals(ss)) {
                return true;
            }
        }
        return false;
    }

    public void refreshSongList() {
        songFragment.adapter.notifyDataSetChanged();
        serviceMusic.setSongList(Config.localSongList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Start service
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            mApp.setIsServiceRunning(true);
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver((mReceiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));

        if (mApp.isServiceRunning() && mApp.getService() != null) {
            String[] updateFlags = new String[]{Common.UPDATE_PAGER_POSTIION,
                    Common.UPDATE_SEEKBAR_DURATION,
                    Common.HIDE_STREAMING_BAR,
                    Common.INIT_PAGER,
                    Common.UPDATE_PLAYBACK_CONTROLS,
                    Common.UPDATE_EQ_FRAGMENT};

            String[] flagValues = new String[]{"" + mApp.getService().getCurrentSongIndex(),
                    "" + mApp.getService().getMediaPlayer().getDuration() / 1000,
                    "", "", "", ""};
            mApp.broadcastUpdateUICommand(updateFlags, flagValues);
        }
    }

    @Override
    protected void onDestroy() {
        //Stop service
        unbindService(musicConnection);
        stopService(playIntent);
        serviceMusic = null;
        mApp.setIsServiceRunning(false);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void setSelectedSong(int position, int notificationId) {
        serviceMusic.setSelectedSong(position, notificationId);

    }

    @OnClick(R.id.playing_next)
    void nextSong() {
        serviceMusic.nextSong();
    }

    @OnClick(R.id.playing_play)
    void playPauseSong() {
        playing_play.startAnimation(serviceMusic.getMediaPlayer().isPlaying());
        serviceMusic.playPauseSong();
    }

    @OnClick(R.id.playing_previous)
    void previousSong() {
        serviceMusic.previousSong();
    }

    @OnClick(R.id.playing_repeat)
    void changeRepeatType() {
        if (mApp.getService().getCurrentRepeatType() == RepeatType.REPEAT_ALL) {
            playing_repeat.startAnimation(R.drawable.ic_repeat_one);
            mApp.getService().setCurrentRepeatType(RepeatType.REPEAT_ONE);
        } else if (mApp.getService().getCurrentRepeatType() == RepeatType.SHUFFLE) {
            playing_repeat.startAnimation(R.drawable.ic_repeat_all);
            mApp.getService().setCurrentRepeatType(RepeatType.REPEAT_ALL);
        } else if (mApp.getService().getCurrentRepeatType() == RepeatType.REPEAT_ONE) {
            playing_repeat.startAnimation(R.drawable.ic_shuffle);
            mApp.getService().setCurrentRepeatType(RepeatType.SHUFFLE);
        }
    }

    public void setSliderDuration(int duration) {
        playing_slider.setValueRange(0, duration, true);
        playing_slider.setValue(mApp.getService().getMediaPlayer().getCurrentPosition() / 1000, true);
        mHandler.postDelayed(sliderUpdateRunnable, 1000);
    }

    public void setPlayPauseButton() {
        if (mApp.isServiceRunning()) {
            if (mApp.getService().isPlayingMusic()) {
                playing_play.startAnimation(R.drawable.ic_pause);
                playing_slider.setAnimation(null);
            } else {
                playing_play.startAnimation(R.drawable.ic_play);
                startSliderStrobeEffect();
            }

        }

    }

    private void startSliderStrobeEffect() {
        mSeekbarStrobeAnim = new AlphaAnimation(1.0f, 0.0f);
        mSeekbarStrobeAnim.setRepeatCount(Animation.INFINITE);
        mSeekbarStrobeAnim.setDuration(700);
        mSeekbarStrobeAnim.setRepeatMode(Animation.REVERSE);

        playing_slider.startAnimation(mSeekbarStrobeAnim);

    }

    public void setRepeatButtonIcon() {
        if (mApp.isServiceRunning())
            if (mApp.getService().getCurrentRepeatType() == RepeatType.REPEAT_ALL) {
                playing_repeat.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_all));
            } else if (mApp.getService().getCurrentRepeatType() == RepeatType.REPEAT_ONE) {
                playing_repeat.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_one));
            } else if (mApp.getService().getCurrentRepeatType() == RepeatType.SHUFFLE) {
                playing_repeat.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_shuffle));
            }
    }

    public void updateUI(int currentIndex) {
        Song song = Config.localSongList.get(currentIndex);
        Bitmap songBitmap = Config.StringToBitMap(song.getBitmapString());
        playing_background.setImageBitmap(songBitmap);
        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

        playing_song_name.setText(song.getName());
        playing_song_artist.setText(song.getArtist());
        playing_song_thumbnail.setImageBitmap(songBitmap);
        setPlayPauseButton();
        setSliderDuration(song.getLength());
    }
}
