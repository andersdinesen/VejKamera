package com.vejkamera.map;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.details.RoadCameraDetailsActivity;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraReadRequest;

import java.util.ArrayList;
import java.util.List;


public class MapCamerasListActivity extends AppCompatActivity implements OnMapReadyCallback {
    public final static String MAP_READ_REQUEST_KEY = "MAP_READ_REQUEST";
    ArrayAdapter<RoadCamera> adapter;
    List<RoadCamera> cameraList = new ArrayList();
    RoadCameraReadRequest readRequest;
    private CameraImagesResponseReceiver cameraImageResponseReceiver;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readRequest = getIntent().getParcelableExtra(MAP_READ_REQUEST_KEY);
        setContentView(R.layout.activity_map_camera_list);

        setupToolBar();
        setupReadRequest();
        readAreaCamerasList();
        setupAdapter();
        readMapMarkerCamerasImages();

        setupListListener();

        setupMapFragment();
    }

    private void setupReadRequest(){
        readRequest.setThumbNailsOnly(true);
    }

    private void setupToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_map_list);
        String toolbarTitle = "";
        for(RoadCamera roadCamera :readRequest.getRequestedRoadCameras(this)) {
            toolbarTitle = toolbarTitle + (toolbarTitle.length() == 0 ? "" : " / ") + roadCamera.getTitle();
        }
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);
    }

    private void setupAdapter() {
        final ListView camerasListView = (ListView) findViewById(R.id.map_camera_info_listviewXXX);
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
        final ListView areaCamerasListView = (ListView) findViewById(R.id.map_camera_info_listviewXXX);

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

    private void setupMapFragment() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_for_multi_marker);
        mapFragment.getView().setClickable(false);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.getUiSettings().setAllGesturesEnabled(false);
        addMarker();
        moveCamera();
    }

    private void addMarker(){
        RoadCamera camera = cameraList.get(0);
        LatLng latLng = new LatLng(camera.getLatitude(), camera.getLongitude());
        Marker newMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(camera.getTitle()));
        switch (cameraList.size()){
            case 2:
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin_2markers));
                break;
            case 3:
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin_3markers));
                break;
            case 4:
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin_4markers));
                break;
        }
    }

    private void moveCamera(){
        final View mapView = getFragmentManager().findFragmentById(R.id.map_for_multi_marker).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    RoadCamera camera = cameraList.get(0);
                    LatLng latLng = new LatLng(camera.getLatitude(), camera.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                }
            });
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
