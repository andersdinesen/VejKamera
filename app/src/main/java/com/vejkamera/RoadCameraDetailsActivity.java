package com.vejkamera;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


public class RoadCameraDetailsActivity extends AppCompatActivity {
    public final static String ROAD_CAMERA_KEY = "ROAD_CAMERA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_camera_details);

        RoadCamera roadCamera = (RoadCamera) getIntent().getParcelableExtra(ROAD_CAMERA_KEY);

        setTitle(roadCamera.getTitle());

        ImageView imageView = (ImageView) findViewById(R.id.detailed_image);
        imageView.setImageBitmap(roadCamera.getBitmap());

        TextView textView = (TextView) findViewById(R.id.detailed_description);
        textView.setText(roadCamera.getInfo());
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
}
