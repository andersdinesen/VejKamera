package com.vejkamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class RoadCameraDetailsActivity extends AppCompatActivity {
    public final static String ROAD_CAMERA_KEY = "ROAD_CAMERA";
    RoadCamera roadCamera = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_camera_details);

        roadCamera = (RoadCamera) getIntent().getParcelableExtra(ROAD_CAMERA_KEY);

        setTitle(roadCamera.getTitle());

        ImageView imageView = (ImageView) findViewById(R.id.detailed_image);
        imageView.setImageBitmap(roadCamera.getBitmap());

        TextView textView = (TextView) findViewById(R.id.detailed_description);
        String info = roadCamera.getInfo();
        textView.setText(roadCamera.getInfo());

        updateCameraImage();
    }

    private void updateCameraImage(){
        LocalBroadcastManager.getInstance(this).registerReceiver(new CameraImagesResponseReceiver(), new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE));

        Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);
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
