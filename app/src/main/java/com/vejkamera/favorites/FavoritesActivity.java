package com.vejkamera.favorites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.vejkamera.area.AreasListActivity;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.details.RoadCameraDetailsActivity;
import com.vejkamera.favorites.adapter.NavDrawerItem;
import com.vejkamera.favorites.adapter.NavDrawerListAdapter;
import com.vejkamera.map.RoadCamersMapsActivity;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraLoopReaderService;

import java.util.ArrayList;


public class FavoritesActivity extends AppCompatActivity {
    ArrayAdapter<RoadCamera> adapter;
    ArrayList<RoadCamera> favorites = new ArrayList<>();
    FavoritesResponseReceiver favoritesResponseReceiver = new FavoritesResponseReceiver();
    Intent readIntent = null;
    private ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent = new Intent(this, RoadCameraLoopReaderService.class);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        setupDrawerMenu();
        setupAdapter();
    }

    private void setupDrawerMenu() {
        final CharSequence mTitle = getTitle();
        final CharSequence mDrawerTitle = mTitle;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        NavDrawerListAdapter drawerListAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(drawerListAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

    }


    @Override
    protected void onResume() {
        super.onResume();
        updateFavorites();
        readFavoriteCameras();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(favoritesResponseReceiver);
        // For some reason a waiting service does not stop on stopService. Broadcasting stop intent instead.
        //stopService(readIntent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_STOP));
    }

    private void updateFavorites() {
        favorites.clear();
        favorites.addAll(RoadCameraFavoritesHandler.getFavorites(this));
        /*
        favorites.add(new RoadCamera("E20 Lilleb\u00E6ldt", "http://webcam.trafikken.dk/webcam/VejleN_Horsensvej_Cam1.jpg", null));
        favorites.add(new RoadCamera("E20 Kauslunde V", "http://webcam.trafikken.dk/webcam/kauslunde2.jpg", null));*/
    }

    private void setupAdapter() {
        final RecyclerView listView = (RecyclerView) findViewById(R.id.favorites_listview);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FavoriteListAdapter(this, favorites);
        listView.setAdapter(adapter);
        setupListner(listView);
    }

    private void readFavoriteCameras() {
        // Prepare for receiving the result when the favorites are read
        IntentFilter intentFilter = new IntentFilter(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(favoritesResponseReceiver, intentFilter);

        //Start service to read favorites
        readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, favorites);
        //bindService(readIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(readIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    private void setupListner(final ListView cityListView) {
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                                @Override
                                                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                                                    final RoadCamera roadCamera = (RoadCamera) parent.getItemAtPosition(position);

                                                    //Setting Camera image to null, because it may be too big for the internal parcel bundle
                                                    roadCamera.setBitmap(null);
                                                    Intent intent = new Intent(parent.getContext(), RoadCameraDetailsActivity.class);
                                                    intent.putExtra(RoadCameraDetailsActivity.ROAD_CAMERA_KEY, roadCamera);
                                                    startActivity(intent);
                                                }
                                            }

        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_by_list) {
            Intent intent = new Intent(this, AreasListActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_by_map) {
            Intent intent = new Intent(this, RoadCamersMapsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class FavoritesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            favorites.clear();

            ArrayList<RoadCamera> updatedFavorites = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            //TODO: Check if this look in really needed, can we set favorites = updatedFavorites
            favorites.addAll(updatedFavorites);
            adapter.notifyDataSetChanged();

        }
    }

    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // display view for selected nav drawer item
            switch (position) {
                case 1:
                    Intent intent = new Intent(parent.getContext(), AreasListActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
}


