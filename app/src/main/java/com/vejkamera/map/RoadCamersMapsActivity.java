package com.vejkamera.map;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.details.RoadCameraDetailsActivity;
import com.vejkamera.favorites.RoadCameraArchiveHandler;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraListingReaderService;
import com.vejkamera.services.RoadCameraReadRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoadCamersMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    List<RoadCamera> cameraList = new ArrayList();
    HashMap<Marker, RoadCamera> markerToRoadCameras = new HashMap<>();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_camers_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new MapCameraInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                              @Override
                                              public void onInfoWindowClick(Marker marker) {
                                                  RoadCamera roadCamera = markerToRoadCameras.get(marker);
                                                  Intent intent = new Intent(getBaseContext(), RoadCameraDetailsActivity.class);
                                                  RoadCameraReadRequest readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS, roadCamera.getSyncId());
                                                  intent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
                                                  startActivity(intent);
                                              }
                                          }
        );

        readAllCameras();
        moveMapToDK();
    }

    private void addCameraMarkers() {
        for (int i=0; i<cameraList.size(); i++) {
            RoadCamera camera = cameraList.get(i);
            LatLng letLng = new LatLng(camera.getLatitude(), camera.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(letLng)
                    .title(camera.getTitle()));
            if(RoadCameraArchiveHandler.getRoadCameraAtSamePosition(camera) != null) {
                switch (RoadCameraArchiveHandler.getRoadCameraAtSamePosition(camera).size()){
                    case 1:
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin_2markers));
                        break;
                    case 2:
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin_3markers));
                        break;
                    case 3:
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin_4markers));
                        break;
                    case 4:
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin_5markers));
                        break;
                }
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(getMapPinIconFromRoadCamera(camera)));
            }
            markerToRoadCameras.put(marker, camera);
        }
    }

    private int getMapPinIconFromRoadCamera(RoadCamera roadCamera){
        if (roadCamera.getDirection()>337.5 || (roadCamera.getDirection()>0 && roadCamera.getDirection()<22.5) ){
            return R.drawable.app_icon_map_pin_000;
        } else if (roadCamera.getDirection() < 67.5){
            return R.drawable.app_icon_map_pin_045;
        } else if (roadCamera.getDirection() < 112.5){
            return R.drawable.app_icon_map_pin_090;
        } else if (roadCamera.getDirection() < 157.5){
            return R.drawable.app_icon_map_pin_135;
        } else if (roadCamera.getDirection() < 202.5){
            return R.drawable.app_icon_map_pin_180;
        } else if (roadCamera.getDirection() < 247.5){
            return R.drawable.app_icon_map_pin_225;
        } else if (roadCamera.getDirection() < 292.5){
            return R.drawable.app_icon_map_pin_270;
        } else if (roadCamera.getDirection() < 337.5){
            return R.drawable.app_icon_map_pin_315;
        } else if (roadCamera.getDirection() == -1){
            return getCameraDirectionFromInfo(roadCamera);
        } else {
            return R.drawable.app_icon_map_pin_090;
        }
    }

    private int getCameraDirectionFromInfo(RoadCamera roadCamera){
        if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.northeast_info_search))){
            return R.drawable.app_icon_map_pin_045;
        } else if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.southheast_info_search))){
            return R.drawable.app_icon_map_pin_135;
        } else if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.southwest_info_search))){
            return R.drawable.app_icon_map_pin_225;
        } else if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.northwest_info_search))){
            return R.drawable.app_icon_map_pin_315;
        } else if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.north_info_search))){
            return R.drawable.app_icon_map_pin_000;
        } else if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.east_info_search))){
            return R.drawable.app_icon_map_pin_090;
        } else if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.south_info_search))){
            return R.drawable.app_icon_map_pin_180;
        } else if(roadCamera.getInfo().toUpperCase().contains(getString(R.string.west_info_search))){
            return R.drawable.app_icon_map_pin_270;
        } else {
            return R.drawable.app_icon_map_pin_090;
        }
    }

    private void readAllCameras() {
        if(!RoadCameraArchiveHandler.isDoneReadingCameraList()) {
            CameraListingResponseReceiver cameraListingResponseReceiver = new CameraListingResponseReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(cameraListingResponseReceiver, new IntentFilter(RoadCameraListingReaderService.BROADCAST_LIST_READING_DONE));
            // If list of road cameras are not all ready read, then wait for them. Reading started in FavoritesActivity
            if(RoadCameraArchiveHandler.isDoneReadingCameraList()) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(cameraListingResponseReceiver);
                cameraList = RoadCameraArchiveHandler.getAllRoadCameras(getBaseContext());
                addCameraMarkers();
            }
        } else {
            cameraList = RoadCameraArchiveHandler.getAllRoadCameras(getBaseContext());
            addCameraMarkers();
        }
    }

    private void moveMapToDK() {
        final View mapView = getFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                    String[] coordinatesStrings = getResources().getStringArray(R.array.dk_bounds);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (int i = 0; i < coordinatesStrings.length; i++) {
                        String[] pointArray = coordinatesStrings[i].split(",");
                        builder.include(new LatLng(Double.valueOf(pointArray[0].trim()), Double.valueOf(pointArray[1].trim())));
                    }
                    LatLngBounds bounds = builder.build();

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                }
            });
        }
    }
/*
    private class CameraImagesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            cameraList.clear();

            ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            cameraList.addAll(updatedCameras);
            addCameraMarkers();
            //adapter.notifyDataSetChanged();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }
*/
    private class CameraListingResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            cameraList.clear();
            //ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            cameraList.addAll(RoadCameraArchiveHandler.getAllRoadCameras(getBaseContext()));
            addCameraMarkers();
            //adapter.notifyDataSetChanged();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }


    private class MapCameraInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mapCameraContent;
        private RoadCamera roadCamera;
        ImageView thumbnail0;
        Marker marker;


         MapCameraInfoWindowAdapter() {
            super();
            mapCameraContent = getLayoutInflater().inflate(R.layout.map_camera_info, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // This means that getInfoContents will be called.
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            this.marker = marker;
            roadCamera = markerToRoadCameras.get(marker);
            thumbnail0 = ((ImageView) mapCameraContent.findViewById(R.id.thumbnail_in_map_info_view0));
            TextView title0 = ((TextView) mapCameraContent.findViewById(R.id.title_in_map_info_view0));
            title0.setText(roadCamera.getTitle());

            ImageView mapPin = ((ImageView) mapCameraContent.findViewById(R.id.map_pin_in_map_info_view0));
            mapPin.setImageResource(getMapPinIconFromRoadCamera(roadCamera));

            if(roadCamera.getThumbnail() != null) {
                thumbnail0.setImageBitmap(roadCamera.getThumbnail());
            } else {
                readThumbnailImage(roadCamera);
            }

            return mapCameraContent;
        }

        private void readThumbnailImage(RoadCamera roadCamera){
            Intent readIntent = new Intent(RoadCamersMapsActivity.this, RoadCameraImageReaderService.class);
            RoadCameraReadRequest readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS, roadCamera.getSyncId());
            readRequest.setThumbNailsOnly(true);
            readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
            startService(readIntent);

            IntentFilter intentFilter = new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE);
            LocalBroadcastManager.getInstance(RoadCamersMapsActivity.this).registerReceiver(new CameraImagesResponseReceiver(), intentFilter);
        }

        private class CameraImagesResponseReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                //ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
                RoadCameraReadRequest readRequest = intent.getParcelableExtra(RoadCameraImageReaderService.READ_REQUEST_KEY);
                roadCamera = readRequest.getRequestedRoadCameras(context).get(0);
                thumbnail0.setImageBitmap(roadCamera.getBitmap());
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

                if(marker != null && marker.isInfoWindowShown()) {
                    marker.showInfoWindow();
                }
            }
        }
    }
}
