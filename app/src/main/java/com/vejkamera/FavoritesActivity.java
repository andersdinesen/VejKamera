package com.vejkamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class FavoritesActivity extends AppCompatActivity {
    ArrayAdapter<RoadCamera> adapter;
    ArrayList<RoadCamera> favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        updateFavorites();
        setupAdapter();
        readFavoriteCameras();
    }

    private void updateFavorites() {
        favorites = new ArrayList();
        favorites.add(new RoadCamera("E20 Lilleb\u00E6ldt", "http://webcam.trafikken.dk/webcam/VejleN_Horsensvej_Cam1.jpg", null));
        favorites.add(new RoadCamera("E20 Kauslunde V", "http://webcam.trafikken.dk/webcam/kauslunde2.jpg", null));
    }

    private void setupAdapter(){
        final ListView listView = (ListView) findViewById(R.id.favorites_listview);
        adapter = new FavoriteListAdapter(this, favorites);
        listView.setAdapter(adapter);
    }

    private void readFavoriteCameras() {
        // Prepare for receiving the result when the favorites are read
        FavoritesResponseReceiver favoritesResponseReceiver = new FavoritesResponseReceiver();
        IntentFilter intentFilter = new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(favoritesResponseReceiver, intentFilter);

        //Start service to read favorites
        Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);
        readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, favorites);
        startService(readIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_by_list){
            Intent intent = new Intent(this, AreasListActivity.class);
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
