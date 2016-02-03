package com.vejkamera.details;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.favorites.RoadCameraArchiveHandler;
import com.vejkamera.map.DirectionToMapPin;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraLoopReaderService;
import com.vejkamera.services.RoadCameraReadRequest;


public class RoadCameraDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    public final static String ROAD_CAMERA_KEY = "ROAD_CAMERA";
    public final static String ROAD_CAMERA_SYNC_ID_KEY = "ROAD_CAMERA_SYNC_ID";
    private RoadCamera roadCamera = null;
    private BroadcastReceiver cameraImagebroadcastReceiver = new CameraImagesResponseReceiver();
    Intent readIntent = null;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_camera_details);
        readIntent = new Intent(this, RoadCameraLoopReaderService.class);

        if(getIntent().hasExtra(ROAD_CAMERA_KEY)) {
            roadCamera = getIntent().getParcelableExtra(ROAD_CAMERA_KEY);
        } else {
            RoadCameraReadRequest readRequest = getIntent().getParcelableExtra(RoadCameraImageReaderService.READ_REQUEST_KEY);
            roadCamera = readRequest.getRequestedRoadCameras(getBaseContext()).get(0);
            readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
        }

        setupLayout();
        setupFavoriteCheckBox();
        setupMapFragment();
    }

    private void setupLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_details);
        toolbar.setTitle(roadCamera.getTitle());
        setSupportActionBar(toolbar);

        if(roadCamera.getBitmap() != null) {
            ImageView imageView = (ImageView) findViewById(R.id.detailed_image);
            imageView.setImageBitmap(roadCamera.getBitmap());
        } else if(roadCamera.getThumbnail() != null) {
            ImageView imageView = (ImageView) findViewById(R.id.detailed_image);
            imageView.setImageBitmap(roadCamera.getBitmap());
        }

        TextView textView = (TextView) findViewById(R.id.detailed_description);
        textView.setText(roadCamera.getInfo());
    }

    private void setupFavoriteCheckBox(){
        CheckBox favoriteCheckBox = (CheckBox) findViewById(R.id.detailed_star);

        favoriteCheckBox.setChecked(RoadCameraArchiveHandler.isFavorite(roadCamera, this));

        favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    RoadCameraArchiveHandler.addFavorite(roadCamera, buttonView.getContext());
                } else {
                    RoadCameraArchiveHandler.removeFavorite(roadCamera, buttonView.getContext());
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startImageUpdatingService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(readIntent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(cameraImagebroadcastReceiver);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_WAKE_UP));
    }

    private void startImageUpdatingService(){
        IntentFilter intentFilter = new IntentFilter(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(cameraImagebroadcastReceiver, intentFilter);

        //Start service to read favorites
        startService(readIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_road_camera_details, menu);
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

    private class CameraImagesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            RoadCameraReadRequest readRequest = intent.getParcelableExtra(RoadCameraImageReaderService.READ_REQUEST_KEY);
            roadCamera = readRequest.getRequestedRoadCameras(context).get(0);
            ImageView cameraImage = (ImageView) findViewById(R.id.detailed_image);
            cameraImage.setImageBitmap(roadCamera.getBitmap());
            //LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }

    private void setupMapFragment() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_for_details_marker);
        mapFragment.getView().setClickable(false);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.getUiSettings().setAllGesturesEnabled(false);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_for_details_marker);
        mapFragment.getView().setClickable(false);

        addMarker();
        moveCamera();
    }

    private void addMarker(){
        LatLng latLng = new LatLng(roadCamera.getLatitude(), roadCamera.getLongitude());
        Marker newMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(roadCamera.getTitle()));
        newMarker.setIcon(BitmapDescriptorFactory.fromResource(DirectionToMapPin.getMapPinIconFromRoadCamera(roadCamera, this)));
    }

    private void moveCamera(){
        final View mapView = getFragmentManager().findFragmentById(R.id.map_for_details_marker).getView();
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

                    LatLng latLng = new LatLng(roadCamera.getLatitude(), roadCamera.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                }
            });
        }
    }

}
