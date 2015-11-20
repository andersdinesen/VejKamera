package com.vejkamera.area;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.vejkamera.Constants;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.details.RoadCameraDetailsActivity;
import com.vejkamera.favorites.RoadCameraArchiveHandler;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraReadRequest;

import java.util.ArrayList;


public class AreaCamerasListActivity extends AppCompatActivity {
    public final static String EXTRA_AREA_NAME_KEY = "AREA_NAME";
    public final static String EXTRA_AREA_POSITION_KEY = "AREA_POSITION";
    ArrayAdapter<RoadCamera> adapter;
    ArrayList<RoadCamera> cameraList = new ArrayList();
    int areaPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        areaPosition = getIntent().getIntExtra(EXTRA_AREA_POSITION_KEY,0);
        setContentView(R.layout.activity_area_camers_list);

        if (getIntent() != null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_area_list);
            toolbar.setTitle(getString(Constants.AREA_IDS[areaPosition]));
            setSupportActionBar(toolbar);
        }

        setupAdapter();
        readAreaCameras();

        setupListListner();
    }

    private void setupAdapter(){
        final ListView camerasListView = (ListView) findViewById(R.id.area_cameras_listview);
        adapter = new AreaCameraListAdapter(this, cameraList);
        camerasListView.setAdapter(adapter);
    }

    private void readAreaCameras(){
        LocalBroadcastManager.getInstance(this).registerReceiver(new CameraImagesResponseReceiver(), new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));

        Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);
        readIntent.putExtra(RoadCameraImageReaderService.THUMBNAILS_ONLY_KEY, "Y");

        ArrayList<String> areaSyncIds;
        RoadCameraReadRequest readRequest;
        if(Constants.AREA_IDS[areaPosition] == R.string.all_areas){
            readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_ALL);
        } else {
            areaSyncIds = RoadCameraArchiveHandler.getSyncIdsFromRoadCameras(RoadCameraArchiveHandler.filterListOfCameras(Constants.AREA_IDS[areaPosition], this));
            readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS, areaSyncIds);
        }

        readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
        //readIntent.putExtra(RoadCameraImageReaderService.AREA_CAMERA_ID_KEY, Constants.AREA_IDS[areaPosition]);
        //readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, cameraList);
        startService(readIntent);

        /*
        LocalBroadcastManager.getInstance(this).registerReceiver(new CameraListingResponseReceiver(), new IntentFilter(RoadCameraListingReaderService.BROADCAST_LIST_READING_DONE));

        Intent listReadIntent = new Intent(this, RoadCameraListingReaderService.class);
        startService(listReadIntent);
        */
    }


    private void setupListListner() {
        final ListView areaCamerasListView = (ListView) findViewById(R.id.area_cameras_listview);

        areaCamerasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                                       @Override
                                                       public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                                                           final RoadCamera item = (RoadCamera) parent.getItemAtPosition(position);

                                                           Intent intent = new Intent(parent.getContext(), RoadCameraDetailsActivity.class);
                                                           RoadCameraReadRequest readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS, item.getSyncId());
                                                           intent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
                                                           //intent.putExtra(RoadCameraDetailsActivity.ROAD_CAMERA_KEY, item);
                                                           startActivity(intent);

                                                       }
                                                   }

        );
    }
/*
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
*/
    private class CameraImagesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            cameraList.clear();

            ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            cameraList.addAll(updatedCameras);
            adapter.notifyDataSetChanged();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
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
