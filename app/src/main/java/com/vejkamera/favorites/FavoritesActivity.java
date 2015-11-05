package com.vejkamera.favorites;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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

import com.vejkamera.area.AreasListActivity;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.favorites.adapter.FavoriteRecycleListAdapter;
import com.vejkamera.favorites.adapter.NavDrawerItem;
import com.vejkamera.favorites.adapter.NavDrawerListAdapter;
import com.vejkamera.map.RoadCamersMapsActivity;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraLoopReaderService;

import java.util.ArrayList;


public class FavoritesActivity extends AppCompatActivity {
    FavoriteRecycleListAdapter adapter;
    ArrayList<RoadCamera> favorites = new ArrayList<>();
    FavoritesResponseReceiver favoritesResponseReceiver = new FavoritesResponseReceiver();
    Intent readIntent = null;
    AlertDialog.Builder addByDialogBuilder;
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
        favorites.addAll(RoadCameraFavoritesHandler.getFavorites(this));

        //favorites.add(new RoadCamera("E20 Lilleb\u00E6ldt", "http://webcam.trafikken.dk/webcam/VejleN_Horsensvej_Cam1.jpg", null));
        //favorites.add(new RoadCamera("E20 Kauslunde V", "http://webcam.trafikken.dk/webcam/kauslunde2.jpg", null));
    }

    private void setupRecycleAdapter() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorites_listview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new FavoriteRecycleListAdapter(favorites);
        recyclerView.setAdapter(adapter);
        setupFloatingButtonListener();
    }

    private void setupFloatingButtonListener(){
        final NavDrawerItem[] addByItems = {new NavDrawerItem(getString(R.string.add_by_map), R.drawable.ic_add_by_location_24dp),
                new NavDrawerItem(getString(R.string.add_from_lists), R.drawable.ic_playlist_add_black_24dp)};

        ListAdapter addByAdapter = new ArrayAdapter<NavDrawerItem>(
                this,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                addByItems){
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

/*
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
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menu_grid_layout) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorites_listview);

            int currentLayout = RoadCameraFavoritesHandler.getFavoritesGridLayout(this);
            int newLayout = currentLayout%3 + 1;

            recyclerView.setLayoutManager(new GridLayoutManager(this, newLayout));
            RoadCameraFavoritesHandler.setFavoritesGridLayout(newLayout, this);

            if(newLayout == 1){

            }

        } else if (id == R.id.menu_sorting) {
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
            for (int i=0; i<favorites.size() ; i++) {
                adapter.notifyItemChanged(i);
            }
            //adapter.notifyDataSetChanged();

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


