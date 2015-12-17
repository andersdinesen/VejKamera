package com.vejkamera.favorites;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
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
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.vejkamera.area.AreasListActivity;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.favorites.adapter.FavoriteRecycleListAdapter;
import com.vejkamera.favorites.adapter.NavDrawerItem;
import com.vejkamera.favorites.adapter.NavDrawerItemAction;
import com.vejkamera.favorites.adapter.NavDrawerItemHeading;
import com.vejkamera.favorites.adapter.NavDrawerProfileLine;
import com.vejkamera.favorites.adapter.NavDrawerItemMainHeading;
import com.vejkamera.favorites.adapter.NavDrawerListAdapter;
import com.vejkamera.map.RoadCamersMapsActivity;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraLoopReaderService;
import com.vejkamera.services.RoadCameraReadRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FavoritesActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    FavoriteRecycleListAdapter recycleListAdapter;
    List<RoadCamera> favorites = null;// new ArrayList<>();
    FavoritesResponseReceiver favoritesResponseReceiver = new FavoritesResponseReceiver();
    ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>();
    NavDrawerListAdapter drawerListAdapter;
    //Intent readIntent = null;
    AlertDialog.Builder addByDialogBuilder;
    private ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;
    ListView drawerList;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Sorting currentSorting = Sorting.BY_ADDED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //readIntent = new Intent(this, RoadCameraLoopReaderService.class);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        RoadCameraArchiveHandler.initRoadCamerasArchive(this);
        updateFavorites();
        setupDrawerMenu();
        setupRecycleAdapter();
        setupFloatingButtonListener();
    }

    private void setupDrawerMenu() {
        final CharSequence mTitle = getTitle();
        final CharSequence mDrawerTitle = mTitle;

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.list_slidermenu);
        loadDrawerMenuItems();

        drawerListAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        drawerList.setAdapter(drawerListAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
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
        drawerLayout.setDrawerListener(drawerToggle);

        drawerList.setOnItemClickListener(new SlideMenuClickListener());

    }

    private void loadDrawerMenuItems() {
        // adding nav drawer items to array
        navDrawerItems.clear();
        navDrawerItems.add(new NavDrawerItemMainHeading(R.drawable.app_icon));
        navDrawerItems.add(new NavDrawerItemHeading(getString(R.string.profiles), R.drawable.ic_filter_black_24dp));
        Integer currentProfileId = RoadCameraProfileHandler.getCurrentProfileId(this);
        for(int i : RoadCameraProfileHandler.getAllProfileIds(this)){
            NavDrawerProfileLine newNavDrawerProfileLine = new NavDrawerProfileLine(RoadCameraProfileHandler.getProfileName(i, this), i);
            navDrawerItems.add(newNavDrawerProfileLine);
            if(currentProfileId != null && i == currentProfileId){
                newNavDrawerProfileLine.setIsSelected(true);
            }
        }
        navDrawerItems.add(new NavDrawerItemAction(getString(R.string.add_profile), R.drawable.ic_add_circle_outline_black_24dp));
        navDrawerItems.add(new NavDrawerItemAction(getString(R.string.remove_profile), R.drawable.ic_remove_circle_outline_black_24dp));
        navDrawerItems.add(new NavDrawerItemAction(getString(R.string.rename_profile), R.drawable.ic_code_black_24dp));
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        startReadingFavorites();
    }

    private void startReadingFavorites(){
        updateFavorites();
        readFavoriteCameras();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReadingFavorites();
    }

    private void stopReadingFavorites(){
        LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(favoritesResponseReceiver);
        wakeUpFavoritesReaderService(true);
        /*
        Intent stopReadingFavorites = new Intent(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_WAKE_UP);
        stopReadingFavorites.putExtra(RoadCameraLoopReaderService.STOP_READING_KEY, "Y");
        LocalBroadcastManager.getInstance(this).sendBroadcast(stopReadingFavorites);*/
    }

    private void wakeUpFavoritesReaderService(boolean stopReading){
        // For some reason a waiting service does not stop on stopService. Broadcasting stop intent instead.
        //stopService(readIntent);
        Intent stopReadingFavorites = new Intent(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_WAKE_UP);
        stopReadingFavorites.putExtra(RoadCameraLoopReaderService.STOP_READING_KEY, (stopReading ? "Y" : "N"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(stopReadingFavorites);
    }

    private void updateFavorites() {
        //favorites.clear();
        favorites = (RoadCameraArchiveHandler.getFavorites(this));

        //favorites.add(new RoadCamera("E20 Lilleb\u00E6ldt", "http://webcam.trafikken.dk/webcam/VejleN_Horsensvej_Cam1.jpg", null));
        //favorites.add(new RoadCamera("E20 Kauslunde V", "http://webcam.trafikken.dk/webcam/kauslunde2.jpg", null));
    }

    private void setupRecycleAdapter() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorites_listview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, RoadCameraArchiveHandler.getFavoritesGridLayout(this)));

        recycleListAdapter = new FavoriteRecycleListAdapter(favorites);
        recyclerView.setAdapter(recycleListAdapter);
    }

    private void setupFloatingButtonListener(){
        final NavDrawerItemHeading[] addByItems = {new NavDrawerItemHeading(getString(R.string.add_by_map), R.drawable.ic_add_by_location_24dp),
                new NavDrawerItemHeading(getString(R.string.add_from_lists), R.drawable.ic_playlist_add_black_24dp)};

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
        Intent readIntent = new Intent(this, RoadCameraLoopReaderService.class);
        readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_FAVORITES));
        startService(readIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    @TargetApi(21)
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menu_grid_layout) {
            changeGridLayout();
        } else if (id == R.id.menu_sorting) {
            final String[] sort_options = {getString(R.string.sort_by_alpha), getString(R.string.sort_by_near), getString(R.string.sort_by_added)};
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

        adjustGridLayoutIcon();
    }

    private void adjustGridLayoutIcon() {
        int gridLayout = RoadCameraArchiveHandler.getFavoritesGridLayout(this);
        MenuView.ItemView menuItem = (MenuView.ItemView) findViewById(R.id.menu_grid_layout);
        if(menuItem!=null) {
            switch (gridLayout) {
                case 1:
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        menuItem.setIcon(getDrawable(R.drawable.ic_view_grid2_white_24dp));
                    } else {
                        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_view_grid2_white_24dp));
                    }
                    break;
                case 2:
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        menuItem.setIcon(getDrawable(R.drawable.ic_view_grid3_white_24dp));
                    } else {
                        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_view_grid3_white_24dp));
                    }
                    break;
                case 3:
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        menuItem.setIcon(getDrawable(R.drawable.ic_view_grid1_white_24dp));
                    } else {
                        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_view_grid1_white_24dp));
                    }
                    break;
            }
        }
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    /*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        //boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
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
        drawerToggle.syncState();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
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
            //favorites.clear();

            //ArrayList<RoadCamera> updatedFavorites = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            //TODO: Check if this look in really needed, can we set favorites = updatedFavorites
            //favorites.addAll(RoadCameraArchiveHandler.getFavorites(context));
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

            NavDrawerItem navDrawerItem =  (NavDrawerItem) parent.getItemAtPosition(position);
            if(navDrawerItem instanceof NavDrawerItemAction){
                handleNavDrawerActionClick((NavDrawerItemAction) navDrawerItem, view.getContext());
            }
            else if(navDrawerItem instanceof NavDrawerProfileLine){
                handleNavDrawerProfileClick((NavDrawerProfileLine) navDrawerItem, view);
            }
        }

        private void handleNavDrawerProfileClick(NavDrawerProfileLine navDrawerProfileLine, final View view){
            // Remove selection from current line
            for(NavDrawerItem listNavDrawerItem : navDrawerItems){
                if(listNavDrawerItem instanceof NavDrawerProfileLine){
                    ((NavDrawerProfileLine)listNavDrawerItem).handleNotSelected();
                }
            }

            // Handle select of profile
            RoadCameraProfileHandler.changeCurrentProfile(navDrawerProfileLine.getProfileId(), view.getContext());
            navDrawerProfileLine.handleSelected();

            updateFavorites();
            wakeUpFavoritesReaderService(false);
            drawerLayout.closeDrawer(drawerList);
        }

        private void handleNavDrawerActionClick(NavDrawerItemAction navDrawerItemAction, final Context context){
            if(navDrawerItemAction.getTitle().equalsIgnoreCase(getString(R.string.add_profile))){
                showAddProfileDialog();
            } else if (navDrawerItemAction.getTitle().equalsIgnoreCase(getString(R.string.remove_profile))){
                showRemoveProfileDialog();
            } else if (navDrawerItemAction.getTitle().equalsIgnoreCase(getString(R.string.rename_profile))){
                showRenameProfileDialog();
            }
        }

        private void showAddProfileDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(FavoritesActivity.this);
            builder.setTitle(getString(R.string.add_profile));

            final EditText input = new EditText(FavoritesActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newProfileName = input.getText().toString();
                    int newProfileId = RoadCameraProfileHandler.createNewProfile(newProfileName, FavoritesActivity.this);
                    RoadCameraProfileHandler.changeCurrentProfile(newProfileId, FavoritesActivity.this);
                    refreshNavDrawer();
                }
            });
            builder.setNegativeButton(android.R.string.cancel , new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        private void showRemoveProfileDialog(){
            if(RoadCameraProfileHandler.getAllProfileIds(FavoritesActivity.this).size()<=1) {
                Toast.makeText(FavoritesActivity.this, R.string.min_1_profile, Toast.LENGTH_SHORT);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(FavoritesActivity.this);
                final int deleteProfileId = RoadCameraProfileHandler.getCurrentProfileId(FavoritesActivity.this);
                builder.setMessage(getString(R.string.remove_profile_with_name) + " " + RoadCameraProfileHandler.getProfileName(deleteProfileId, FavoritesActivity.this));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RoadCameraProfileHandler.removeProfile(deleteProfileId, FavoritesActivity.this);
                        refreshNavDrawer();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        }

        private void showRenameProfileDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(FavoritesActivity.this);
            builder.setTitle(getString(R.string.rename_profile_with_name));

            final EditText input = new EditText(FavoritesActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(RoadCameraProfileHandler.getProfileName(RoadCameraProfileHandler.getCurrentProfileId(FavoritesActivity.this), FavoritesActivity.this));
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newProfileName = input.getText().toString();
                    RoadCameraProfileHandler.setCurrentProfileName(newProfileName, FavoritesActivity.this);
                    refreshNavDrawer();
                }
            });
            builder.setNegativeButton(android.R.string.cancel , new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }


        private void refreshNavDrawer() {
            wakeUpFavoritesReaderService(false);
            loadDrawerMenuItems();
            // notifyDataSetChanged() not working and resulting in inconsistent behaviour :-(
            //drawerListAdapter.notifyDataSetChanged();
            drawerListAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
            drawerList.setAdapter(drawerListAdapter);
        }
    }
}


