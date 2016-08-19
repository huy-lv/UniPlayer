package com.huylv.uniplayer;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.huylv.uniplayer.base.BaseActivityDrawer;
import com.huylv.uniplayer.model.Song;
import com.huylv.uniplayer.task.BuildLibraryTask;
import com.huylv.uniplayer.task.Config;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends BaseActivityDrawer {
//public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final int MENU_SYNC = 12;
    SongFragment songFragment;
    private MenuItem refreshItem;
    private DatabaseReference songTable;
    private ChildEventListener addSongListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        createFireBase();
        songFragment = new SongFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_main,songFragment).addToBackStack(null).commit();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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

    public void refreshSongList(){
        songFragment.adapter.notifyDataSetChanged();
    }
}
