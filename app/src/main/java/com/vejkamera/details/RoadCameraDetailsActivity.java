package com.vejkamera.details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.favorites.RoadCameraArchiveHandler;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraLoopReaderService;

import java.util.ArrayList;


public class RoadCameraDetailsActivity extends AppCompatActivity {
    public final static String ROAD_CAMERA_KEY = "ROAD_CAMERA";
    public final static String ROAD_CAMERA_SYNC_ID_KEY = "ROAD_CAMERA_SYNC_ID";
    private RoadCamera roadCamera = null;
    private BroadcastReceiver cameraImagebroadcastReceiver = new CameraImagesResponseReceiver();
    Intent readIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_camera_details);

        if(getIntent().hasExtra(ROAD_CAMERA_KEY)) {
            roadCamera = getIntent().getParcelableExtra(ROAD_CAMERA_KEY);
        } else {
            roadCamera = RoadCameraArchiveHandler.getRoadCameraFromSyncId(getIntent().getStringExtra(ROAD_CAMERA_SYNC_ID_KEY), this);
        }
        readIntent = new Intent(this, RoadCameraLoopReaderService.class);

        setupLayout();
        setupFavoriteCheckBox();
    }

    private ArrayList<RoadCamera> getListOfCameras(Intent intent) {
        if (intent.hasExtra(RoadCameraImageReaderService.TYPE_TO_READ_KEY)) {
            String syncId = intent.getStringExtra(RoadCameraImageReaderService.)
            return RoadCameraArchiveHandler.getFavorites(getBaseContext());
        }

        if (intent.hasExtra(ROAD_CAMERA_LIST_KEY)) {
            return intent.getParcelableArrayListExtra(ROAD_CAMERA_LIST_KEY);
        }
    }

    private void setupLayout() {
        setTitle(roadCamera.getTitle());

        if(roadCamera.getBitmap() != null) {
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
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_STOP));
    }

    private void startImageUpdatingService(){
        IntentFilter intentFilter = new IntentFilter(RoadCameraLoopReaderService.BROADCAST_IMAGE_LOOP_READING_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(cameraImagebroadcastReceiver, intentFilter);

        //Start service to read favorites
        ArrayList<RoadCamera> cameraList =  new ArrayList<>(1);
        cameraList.add(roadCamera);
        readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, cameraList);
        startService(readIntent);


        /*
        LocalBroadcastManager.getInstance(this).registerReceiver(cameraImagebroadcastReceiver, new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));

        readIntent = new Intent(this, RoadCameraImageReaderService.class);
        ArrayList<RoadCamera> cameraList = new ArrayList<>(1);
        cameraList.add(roadCamera);
        readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, cameraList);
        startService(readIntent);
        */
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
            ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            roadCamera = updatedCameras.get(0);
            ImageView cameraImage = (ImageView) findViewById(R.id.detailed_image);
            cameraImage.setImageBitmap(roadCamera.getBitmap());
            //LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }
}
