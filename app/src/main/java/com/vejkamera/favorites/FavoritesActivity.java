package com.vejkamera.favorites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent = new Intent(this, RoadCameraLoopReaderService.class);
        setContentView(R.layout.activity_favorites);
        setupDrawerMenu();
        setupAdapter();



    }

    private void setupDrawerMenu() {
        String[] mPlanetTitles = getResources().getStringArray(R.array.planets);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                //R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {            /** Called when a drawer has settled in a completely closed state. */
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            //getActionBar().setTitle(mTitle);
        }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateFavorites();
        readFavoriteCameras();
    }

    @Override
    protected void onPause(){
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

    private void setupAdapter(){
        final ListView listView = (ListView) findViewById(R.id.favorites_listview);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_by_list){
            Intent intent = new Intent(this, AreasListActivity.class);
            startActivity(intent);
        } else  if (id == R.id.action_by_map){
            Intent intent = new Intent(this, RoadCamersMapsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
}
