package com.vejkamera.area;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.vejkamera.Constants;
import com.vejkamera.R;

import java.util.ArrayList;


public class AreasListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        final ListView cityListView = (ListView) findViewById(R.id.city_listview);
        final ArrayList<String> listOfCities = new ArrayList<>();

        for (int i = 0; i< Constants.AREA_IDS.length; i++){
            listOfCities.add(getString(Constants.AREA_IDS[i]));
        }
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfCities);
        cityListView.setAdapter(adapter);

        setupListner(cityListView);
    }

    private void setupListner(final ListView cityListView) {
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                                @Override
                                                public void onItemClick(AdapterView<?> parent, final View view,
                                                                        int position, long id) {
                                                    //final String item = (String) parent.getItemAtPosition(position);
                                                    Intent intent = new Intent(parent.getContext(), AreaCamerasListActivity.class);
                                                    intent.putExtra(AreaCamerasListActivity.EXTRA_AREA_POSITION_KEY, position);
                                                    //intent.putExtra(AreaCamerasListActivity.EXTRA_AREA_NAME_KEY, getString(Constants.CITY_IDS[position]));
                                                    startActivity(intent);
                                                }
                                            }

                );
    }

}
