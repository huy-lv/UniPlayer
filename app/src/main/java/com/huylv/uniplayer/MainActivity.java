package com.huylv.uniplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.huylv.uniplayer.base.BaseActivityDrawer;
import com.huylv.uniplayer.fragment.SongFragment;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.service.MusicService;
import com.huylv.uniplayer.task.BuildLibraryTask;
import com.huylv.uniplayer.task.Config;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends BaseActivityDrawer {
    //public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final int MENU_SYNC = 12;
    SongFragment songFragment;
    private MenuItem refreshItem;
    private DatabaseReference songTable;
    private ChildEventListener addSongListener;
    private Intent playIntent;

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

    SlidingUpPanelLayout.PanelState currentState = SlidingUpPanelLayout.PanelState.COLLAPSED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        createFireBase();
        songFragment = new SongFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_main, songFragment).addToBackStack(null).commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        panelToolbar.setBackgroundColor(Color.TRANSPARENT);
        panelToolbar.setVisibility(View.INVISIBLE);
        sliding_layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                panelToolbar.setVisibility(View.VISIBLE);
                playing_layout.setVisibility(View.VISIBLE);
                playing_layout.setAlpha(1-slideOffset);
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

    private MusicService serviceMusic;
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
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onBackPressed() {
        if(currentState == SlidingUpPanelLayout.PanelState.EXPANDED){
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SYNC:
                startSyncAnim();
                (new BuildLibraryTask(MainActivity.this)).execute();
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
        }
    }

    @Override
    protected void onDestroy() {
        //Stop service
        stopService(playIntent);
        serviceMusic = null;
        super.onDestroy();
    }

    public void setSelectedSong(int position, int notificationId) {
        serviceMusic.setSelectedSong(position, notificationId);
        playing_background.setImageBitmap(Config.StringToBitMap(Config.localSongList.get(position).getBitmapString()));
        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }


}
