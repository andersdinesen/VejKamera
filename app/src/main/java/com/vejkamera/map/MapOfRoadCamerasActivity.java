package com.vejkamera.map;

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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class MapOfRoadCamerasActivity extends FragmentActivity implements OnMapReadyCallback {

    List<RoadCamera> cameraList = new ArrayList();
    HashMap<Marker, RoadCamera> markerToRoadCameras = new HashMap<>();
    private GoogleMap mMap;
    private Marker currentMultiMarker;
    private MapCameraRecycleListAdapter mapCameraRecycleListAdapter;
    MapCameraListAdapter mapHeaderLisAdapter;
    private ArrayList<RoadCamera> selectedRoadCameras = new ArrayList<>();
    String lineSeparator = System.getProperty("line.separator");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_camers_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupHeaderListRoadCameraAdapter();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(mMap != null && currentMultiMarker != null){
            currentMultiMarker.hideInfoWindow();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new MapCameraInfoWindowAdapter());

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedRoadCameras.clear();
                RoadCamera roadCamera = markerToRoadCameras.get(marker);
                selectedRoadCameras.add(roadCamera);
                if (RoadCameraArchiveHandler.isThereOtherRoadCamerasAtSamePosition(roadCamera)) {
                    selectedRoadCameras.addAll(RoadCameraArchiveHandler.getRoadCameraAtSamePosition(roadCamera));
                }
                mapHeaderLisAdapter.notifyDataSetChanged();

                readThumbnailImage(selectedRoadCameras);

/*
                cameraImageResponseReceiver = new CameraImagesResponseReceiver();
                LocalBroadcastManager.getInstance(this).registerReceiver(cameraImageResponseReceiver, new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));

                Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);

                readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
                startService(readIntent);


                Intent intent = new Intent(getBaseContext(), MapCamerasListActivity.class);

                RoadCameraReadRequest readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS);
                readRequest.addRoadCameras(selectedRoadCameras);
                intent.putExtra(MapCamerasListActivity.MAP_READ_REQUEST_KEY, readRequest);

                startActivity(intent);
*/
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedRoadCameras.clear();
                mapHeaderLisAdapter.notifyDataSetChanged();
            }
        });
        /*
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
*/
        readAllCameras();
        moveMapToDK();
    }

    private void addCameraMarkers() {
        for (int i=0; i<cameraList.size(); i++) {
            RoadCamera camera = cameraList.get(i);
            LatLng latLng = new LatLng(camera.getLatitude(), camera.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
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
                marker.setIcon(BitmapDescriptorFactory.fromResource(DirectionToMapPin.getMapPinIconFromRoadCamera(camera, this)));
            }
            markerToRoadCameras.put(marker, camera);
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

    private void setupHeaderListRoadCameraAdapter() {
        ListView headerListView = (ListView) findViewById(R.id.map_header_listView);
        mapHeaderLisAdapter = new MapCameraListAdapter(this, selectedRoadCameras); // new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfCameras);
        headerListView.setAdapter(mapHeaderLisAdapter);

        headerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
/*
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.map_header_recyclerview);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));//new GridLayoutManager(this, RoadCameraArchiveHandler.getFavoritesGridLayout(this)));

        mapCameraRecycleListAdapter = new MapCameraRecycleListAdapter(RoadCameraArchiveHandler.getFavorites(this));//selectedRoadCameras);
        recyclerView.setAdapter(mapCameraRecycleListAdapter);

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Click on the View.OnClickListner", Toast.LENGTH_LONG);
                Log.d("Map", "Click on the View.OnClickListner");
            }
        });
*/
/*
        recycleListAdapter = new FavoriteRecycleListAdapter(favorites);
        recyclerView.setAdapter(recycleListAdapter);*/
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

    private void readThumbnailImage(List<RoadCamera> roadCameras){
        Intent readIntent = new Intent(MapOfRoadCamerasActivity.this, RoadCameraImageReaderService.class);
        RoadCameraReadRequest readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS);
        readRequest.addRoadCameras(roadCameras);
        readRequest.setThumbNailsOnly(true);
        readIntent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
        startService(readIntent);

        IntentFilter intentFilter = new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE);
        LocalBroadcastManager.getInstance(MapOfRoadCamerasActivity.this).registerReceiver(new CameraImagesResponseReceiver(), intentFilter);
    }

    private class CameraImagesResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            RoadCameraReadRequest readRequest = intent.getParcelableExtra(RoadCameraImageReaderService.READ_REQUEST_KEY);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            mapHeaderLisAdapter.notifyDataSetChanged();
        }
    }
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
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mapCameraContent.setLayoutParams(layoutParams);
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
            TextView mapCameraInfoText = (TextView) mapCameraContent.findViewById(R.id.map_camera_info_text);
            mapCameraInfoText.setText(roadCamera.getTitle());
            if (RoadCameraArchiveHandler.isThereOtherRoadCamerasAtSamePosition(roadCamera)) {
                for(RoadCamera currentRoadCamera : RoadCameraArchiveHandler.getRoadCameraAtSamePosition(roadCamera)) {
                    mapCameraInfoText.append(lineSeparator + currentRoadCamera.getTitle());
                }
            }

            return mapCameraContent;
        }




    }
}
