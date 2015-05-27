package com.vejkamera;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;


public class FavoritesActivity extends ListActivity {
    ArrayAdapter<RoadCamera> adapter;
    List<RoadCamera> favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        updateFavorites();
        setupAdapter();
    }

    private void updateFavorites() {
        favorites = new ArrayList();
        favorites.add(new RoadCamera("E20 Lilleb\u00E6ldt", "VejleN_Horsensvej_Cam1.jpg", null));
        favorites.add(new RoadCamera("E20 Kauslunde V", "kauslunde2.jpg", null));
    }

    private void setupAdapter(){
        adapter = new RoadCameraListAdapter(this, favorites);
        setListAdapter(adapter);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
