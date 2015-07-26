package com.vejkamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class AreaCamerasListActivity extends AppCompatActivity {
    public final static String EXTRA_AREA_NAME_KEY = "AREA_NAME";
    ArrayAdapter<RoadCamera> adapter;
    ArrayList<RoadCamera> cameraList = new ArrayList();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_camers_list);
        if (getIntent() != null) {
            setTitle(getIntent().getStringExtra(EXTRA_AREA_NAME_KEY));
        }

        setupAdapter();
        readAreaCameras();

    }

    private void setupAdapter(){
        final ListView camerasListView = (ListView) findViewById(R.id.area_cameras_listview);
        adapter = new AreaCameraListAdapter(this, cameraList);
        camerasListView.setAdapter(adapter);
    }

    private void readAreaCameras(){
        LocalBroadcastManager.getInstance(this).registerReceiver(new CameraListingResponseReceiver(), new IntentFilter(RoadCameraListingReaderService.BROADCAST_LIST_READING_DONE));

        Intent listReadIntent = new Intent(this, RoadCameraListingReaderService.class);
        startService(listReadIntent);
    }

    private class CameraListingResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            cameraList.clear();

            ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraListingReaderService.ROAD_CAMERA_LIST_KEY);
            cameraList.addAll(updatedCameras);
            adapter.notifyDataSetChanged();
            readCameraImages(context);
        }

        private void readCameraImages(Context context){
            LocalBroadcastManager.getInstance(context).registerReceiver(new CameraImagesResponseReceiver(), new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));

            Intent readIntent = new Intent(context, RoadCameraImageReaderService.class);
            readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, cameraList);
            startService(readIntent);
        }
    }

    private class CameraImagesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            cameraList.clear();

            ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            cameraList.addAll(updatedCameras);
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_area_camers_list, menu);
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
