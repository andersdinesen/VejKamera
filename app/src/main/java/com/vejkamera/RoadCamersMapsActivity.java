package com.vejkamera;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vejkamera.services.RoadCameraImageReaderService;

import java.util.ArrayList;
import java.util.HashMap;

public class RoadCamersMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    ArrayList<RoadCamera> cameraList = new ArrayList();
    HashMap<Marker, RoadCamera> markers = new HashMap<>();
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

        readAreaCameras();
        moveMapToDK();
        // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
    }

    private void addCameraMarkers() {
        for (RoadCamera camera : cameraList) {
            LatLng letLng = new LatLng(camera.getLatitude(), camera.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions().position(letLng).title(camera.getTitle()));
            markers.put(marker, camera);
        }
    }

    private void readAreaCameras() {
        LocalBroadcastManager.getInstance(this).registerReceiver(new CameraImagesResponseReceiver(), new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));
        //LocalBroadcastManager.getInstance(this).registerReceiver(new CameraListingResponseReceiver(), new IntentFilter(RoadCameraListingReaderService.BROADCAST_LIST_READING_DONE));

        Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);
        readIntent.putExtra(RoadCameraImageReaderService.THUMBNAILS_ONLY_KEY, "Y");
        startService(readIntent);

        /*
        Intent listReadIntent = new Intent(this, RoadCameraListingReaderService.class);
        startService(listReadIntent);
        */
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

    private class MapCameraInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mapCameraContent;
        private RoadCamera roadCamera;

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
            roadCamera = markers.get(marker);
            ImageView thumbnail = ((ImageView) mapCameraContent.findViewById(R.id.thumbnail));
            TextView title = ((TextView) mapCameraContent.findViewById(R.id.title));
            CheckBox favoriteCheckBox = (CheckBox)  mapCameraContent.findViewById(R.id.map_favorite_star);

            title.setText(roadCamera.getTitle());
            title.setOnClickListener(new MapInfoClicked());

            thumbnail.setImageBitmap(roadCamera.getBitmap());
            thumbnail.setOnClickListener(new MapInfoClicked());

            favoriteCheckBox.setChecked(RoadCameraFavoritesHandler.isFavorite(roadCamera, mapCameraContent.getContext()));

            favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        RoadCameraFavoritesHandler.addFavorite(roadCamera, buttonView.getContext());
                    } else {
                        RoadCameraFavoritesHandler.removeFavorite(roadCamera, buttonView.getContext());
                    }
                }
            });

            return mapCameraContent;
        }

        private class MapInfoClicked implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RoadCameraDetailsActivity.class);
                intent.putExtra(RoadCameraDetailsActivity.ROAD_CAMERA_KEY, roadCamera);
                startActivity(intent);
            }
        }

    }
}
