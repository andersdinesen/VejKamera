package com.vejkamera.favorites;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.vejkamera.area.AreasListActivity;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.favorites.adapter.FavoriteRecycleListAdapter;
import com.vejkamera.favorites.adapter.NavDrawerItem;
import com.vejkamera.favorites.adapter.NavDrawerListAdapter;
import com.vejkamera.map.RoadCamersMapsActivity;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraLoopReaderService;
import com.vejkamera.services.RoadCameraReadRequest;

import java.util.ArrayList;
import java.util.Collections;


public class FavoritesActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    FavoriteRecycleListAdapter recycleListAdapter;
    ArrayList<RoadCamera> favorites = new ArrayList<>();
    FavoritesResponseReceiver favoritesResponseReceiver = new FavoritesResponseReceiver();
    Intent readIntent = null;
    AlertDialog.Builder addByDialogBuilder;
    private ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Sorting currentSorting = Sorting.BY_ADDED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent = new Intent(this, RoadCameraLoopReaderService.class);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        RoadCameraArchiveHandler.initRoadCamerasArchive(this);

        setupDrawerMenu();
        setupRecycleAdapter();
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
        favorites.addAll(RoadCameraArchiveHandler.getFavorites(this));

        //favorites.add(new RoadCamera("E20 Lilleb\u00E6ldt", "http://webcam.trafikken.dk/webcam/VejleN_Horsensvej_Cam1.jpg", null));
        //favorites.add(new RoadCamera("E20 Kauslunde V", "http://webcam.trafikken.dk/webcam/kauslunde2.jpg", null));
    }

    private void setupRecycleAdapter() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorites_listview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, RoadCameraArchiveHandler.getFavoritesGridLayout(this)));

        recycleListAdapter = new FavoriteRecycleListAdapter(favorites);
        recyclerView.setAdapter(recycleListAdapter);
        setupFloatingButtonListener();
    }

    private void setupFloatingButtonListener(){
        final NavDrawerItem[] addByItems = {new NavDrawerItem(getString(R.string.add_by_map), R.drawable.ic_add_by_location_24dp),
                new NavDrawerItem(getString(R.string.add_from_lists), R.drawable.ic_playlist_add_black_24dp)};

        ListAdapter addByAdapter = new ArrayAdapter<NavDrawerItem>(this, android.R.layout.select_dialog_item, android.R.id.text1, addByItems){
            public View getView(int position, View convertView, ViewGroup parent) {
                //Use super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(addByItems[position].getIcon(), 0, 0, 0);

                //Add margin between image and text (support various screen densities)
                int padding = (int) (6 * getResources().getDisplayMetrics().density + 0.8f);
                tv.setCompoundDrawablePadding(padding);

                return v;
            }
        };

        addByDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_favorit))
                .setAdapter(addByAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Intent mapIntent = new Intent(getBaseContext(), RoadCamersMapsActivity.class);
                                startActivity(mapIntent);
                                break;
                            case 1:
                                Intent listIntent = new Intent(getBaseContext(), AreasListActivity.class);
                                startActivity(listIntent);
                                break;                        }
                    }
                });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.add_floating_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addByDialogBuilder.show();
            }
        });
    }

    private void readFavoriteCameras() {
        // Prepare for receiving the result when the favorites are read
        IntentFilter intentFilter = new IntentFilter(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(favoritesResponseReceiver, intentFilter);

        //Start service to read favorites
        //readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, favorites);
        //readIntent.putExtra(RoadCameraImageReaderService.TYPE_TO_READ_KEY, RoadCameraImageReaderService.TYPE_TO_READ_FAVORITES);
        readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_FAVORITES));
        //bindService(readIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(readIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    @TargetApi(21)
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menu_grid_layout) {
            changeGridLayout();
        } else if (id == R.id.menu_sorting) {
            final String[] sort_options = {getString(R.string.sort_by_alfabeth), getString(R.string.sort_by_near), getString(R.string.sort_by_added)};
            AlertDialog.Builder sortByDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.sort_by))
                    .setItems(sort_options, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0:
                                    currentSorting = Sorting.BY_NAME;
                                    break;
                                case 1:
                                    currentSorting = Sorting.BY_NEAR;
                                    break;
                                case 2:
                                    currentSorting = Sorting.BY_ADDED;
                                    break;
                            }
                            sortFavorites();
                        }
                    });
            sortByDialogBuilder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortFavorites(){
        if(currentSorting == Sorting.BY_NAME) {
            Collections.sort(favorites);
        }

        recycleListAdapter.notifyDataSetChanged();
    }

    private void changeGridLayout() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorites_listview);

        int currentLayout = RoadCameraArchiveHandler.getFavoritesGridLayout(this);
        int newLayout = currentLayout%3 + 1;

        recyclerView.setLayoutManager(new GridLayoutManager(this, newLayout));
        RoadCameraArchiveHandler.setFavoritesGridLayout(newLayout, this);

        MenuView.ItemView menuItem = (MenuView.ItemView) findViewById(R.id.menu_grid_layout);
        switch(newLayout){
            case 1:
                if(android.os.Build.VERSION.SDK_INT >= 21) {
                    menuItem.setIcon(getDrawable(R.drawable.ic_view_grid2_white_24dp));
                } else {
                    menuItem.setIcon(getResources().getDrawable(R.drawable.ic_view_grid2_white_24dp));
                }
                break;
            case 2:
                if(android.os.Build.VERSION.SDK_INT >= 21) {
                    menuItem.setIcon(getDrawable(R.drawable.ic_view_grid3_white_24dp));
                } else {
                    menuItem.setIcon(getResources().getDrawable(R.drawable.ic_view_grid3_white_24dp));
                }
                break;
            case 3:
                if(android.os.Build.VERSION.SDK_INT >= 21) {
                    menuItem.setIcon(getDrawable(R.drawable.ic_view_grid1_white_24dp));
                } else {
                    menuItem.setIcon(getResources().getDrawable(R.drawable.ic_view_grid1_white_24dp));
                }
                break;
        }
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    /*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
*/


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

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private enum Sorting {
        BY_NAME, BY_NEAR, BY_ADDED
    }

    private class FavoritesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            favorites.clear();

            ArrayList<RoadCamera> updatedFavorites = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            //TODO: Check if this look in really needed, can we set favorites = updatedFavorites
            favorites.addAll(RoadCameraArchiveHandler.getFavorites(context));
            sortFavorites();
            for (int i=0; i<favorites.size() ; i++) {
                recycleListAdapter.notifyItemChanged(i);
            }
            //recycleListAdapter.notifyDataSetChanged();

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


