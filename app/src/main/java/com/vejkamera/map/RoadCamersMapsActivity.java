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
import com.vejkamera.services.RoadCameraLoopReaderService;
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
        Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
        Matrix matrix = new Matrix();
        matrix.po
        for (RoadCamera camera : cameraList) {
            LatLng letLng = new LatLng(camera.getLatitude(), camera.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(letLng)
                    .title(camera.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.app_icon_map_pin)));
            markerToRoadCameras.put(marker, camera);
        }
    }

    private void readAllCameras() {
        if(cameraList.size() == 0) {
            CameraListingResponseReceiver cameraListingResponseReceiver = new CameraListingResponseReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(cameraListingResponseReceiver, new IntentFilter(RoadCameraListingReaderService.BROADCAST_LIST_READING_DONE));
            cameraList = RoadCameraArchiveHandler.getAllRoadCameras(getBaseContext());
            // If list of road cameras are not all ready read, then wait for them. Reading started in FavoritesActivity
            if(cameraList.size() != 0) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(cameraListingResponseReceiver);
                addCameraMarkers();
            }
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
        ImageView thumbnail;
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
            thumbnail = ((ImageView) mapCameraContent.findViewById(R.id.thumbnail));
            TextView title = ((TextView) mapCameraContent.findViewById(R.id.title));

            title.setText(roadCamera.getTitle());
            if(roadCamera.getThumbnail() != null) {
                thumbnail.setImageBitmap(roadCamera.getThumbnail());
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
                thumbnail.setImageBitmap(roadCamera.getBitmap());
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

                if(marker != null && marker.isInfoWindowShown()) {
                    marker.showInfoWindow();
                }
            }
        }
    }
}
