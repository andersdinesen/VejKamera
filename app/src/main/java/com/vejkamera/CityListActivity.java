package com.vejkamera;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class CityListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        final ListView cityListView = (ListView) findViewById(R.id.city_listview);
        final ArrayList<String> listOfCities = new ArrayList<>();

        getResources().getStringArray(R.array.planets);

        for (int i = 0; i<Constants.CITY_IDS.length; i++){
            listOfCities.add(getString(Constants.CITY_IDS[i]));
        }
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfCities);
        cityListView.setAdapter(adapter);
    }

}
