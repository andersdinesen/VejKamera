package com.vejkamera;

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

import com.vejkamera.services.RoadCameraImageReaderService;

import java.util.ArrayList;


public class RoadCameraDetailsActivity extends AppCompatActivity {
    public final static String ROAD_CAMERA_KEY = "ROAD_CAMERA";
    private RoadCamera roadCamera = null;
    private BroadcastReceiver broadcastReceiver = new CameraImagesResponseReceiver();
    Intent readIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_camera_details);

        roadCamera = (RoadCamera) getIntent().getParcelableExtra(ROAD_CAMERA_KEY);

        setTitle(roadCamera.getTitle());

        ImageView imageView = (ImageView) findViewById(R.id.detailed_image);
        imageView.setImageBitmap(roadCamera.getBitmap());

        TextView textView = (TextView) findViewById(R.id.detailed_description);
        textView.setText(roadCamera.getInfo());

        setupFavoriteCheckBox();
    }

    private void setupFavoriteCheckBox(){
        CheckBox favoriteCheckBox = (CheckBox) findViewById(R.id.detailed_star);

        favoriteCheckBox.setChecked(RoadCameraFavoritesHandler.isFavorite(roadCamera, this));

        favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    RoadCameraFavoritesHandler.addFavorite(roadCamera, buttonView.getContext());
                } else {
                    RoadCameraFavoritesHandler.removeFavorite(roadCamera, buttonView.getContext());
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void startImageUpdatingService(){
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));

        readIntent = new Intent(this, RoadCameraImageReaderService.class);
        ArrayList<RoadCamera> cameraList = new ArrayList<>(1);
        cameraList.add(roadCamera);
        readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, cameraList);
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
            ArrayList<RoadCamera> updatedCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            roadCamera = updatedCameras.get(0);
            ImageView cameraImage = (ImageView) findViewById(R.id.detailed_image);
            cameraImage.setImageBitmap(roadCamera.getBitmap());
        }
    }
}
