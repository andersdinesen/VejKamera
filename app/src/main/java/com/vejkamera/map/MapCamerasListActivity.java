package com.vejkamera.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.details.RoadCameraDetailsActivity;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraReadRequest;

import java.util.ArrayList;
import java.util.List;


public class MapCamerasListActivity extends AppCompatActivity {
    public final static String MAP_READ_REQUEST_KEY = "MAP_READ_REQUEST";
    ArrayAdapter<RoadCamera> adapter;
    List<RoadCamera> cameraList = new ArrayList();
    int areaPosition = 0;
    RoadCameraReadRequest readRequest;
    private CameraImagesResponseReceiver cameraImageResponseReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readRequest = getIntent().getParcelableExtra(MAP_READ_REQUEST_KEY);
        setContentView(R.layout.map_camera_info_list);
/*
        if (getIntent() != null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_area_list);
            toolbar.setTitle(getString(Constants.AREA_IDS[areaPosition]));
            setSupportActionBar(toolbar);
        }
*/

        setupReadRequest();
        readAreaCamerasList();
        setupAdapter();
        readMapMarkerCamerasImages();

        setupListListener();
    }

    private void setupReadRequest(){
        readRequest.setThumbNailsOnly(true);
    }

    private void setupAdapter() {
        final ListView camerasListView = (ListView) findViewById(R.id.map_camera_info_listview);
        adapter = new MapCameraListAdapter(this, cameraList);
        camerasListView.setAdapter(adapter);
    }


    private void readAreaCamerasList() {
        cameraList = readRequest.getRequestedRoadCameras(this);
    }

    private void readMapMarkerCamerasImages() {
        cameraImageResponseReceiver = new CameraImagesResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(cameraImageResponseReceiver, new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));

        Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);

        readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
        startService(readIntent);

    }


    private void setupListListener() {
        final ListView areaCamerasListView = (ListView) findViewById(R.id.map_camera_info_listview);

        areaCamerasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                                       @Override
                                                       public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                                                           final RoadCamera item = (RoadCamera) parent.getItemAtPosition(position);

                                                           Intent intent = new Intent(parent.getContext(), RoadCameraDetailsActivity.class);
                                                           RoadCameraReadRequest readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS, item.getSyncId());
                                                           intent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
                                                           startActivity(intent);

                                                       }
                                                   }

        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }


    private class CameraImagesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            cameraList = (readRequest.getRequestedRoadCameras(context));
            adapter.notifyDataSetChanged();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(cameraImageResponseReceiver);
        }
    }
/*
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
    }*/
}
