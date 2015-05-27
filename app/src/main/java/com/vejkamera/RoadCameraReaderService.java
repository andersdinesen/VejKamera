package com.vejkamera;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Anders on 27-05-2015.
 */
public class RoadCameraReaderService extends IntentService {
    public static final String BROADCAST_READING_DONE = "com.vejkamera.READING_DONE";
    public static final String ROAD_CAMERA_LIST_KEY = "ROAD_CAMERA_LIST";

    public RoadCameraReaderService() {
        super(RoadCameraReaderService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RoadCameraReaderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlPath = getString(R.string.URL_path);
        ArrayList<RoadCamera> roadCameras = intent.getParcelableArrayListExtra(ROAD_CAMERA_LIST_KEY);
        String failedReadings = null;

        for (RoadCamera roadCamera : roadCameras) {
            try {
                URL url = new URL(urlPath + roadCamera.getRemoteFileName());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream is = connection.getInputStream();
                roadCamera.setBitmap(BitmapFactory.decodeStream(is));
            } catch (MalformedURLException e) {
                failedReadings = updateFailedReading(failedReadings, roadCamera, e);
            } catch (IOException e) {
                failedReadings = updateFailedReading(failedReadings, roadCamera, e);
            }
        }

        broadcastResult(roadCameras);
    }

    private String updateFailedReading(String failedReadings, RoadCamera roadCamera, Exception e) {
        failedReadings = (failedReadings != null ? failedReadings + ", " : "") + roadCamera.getDisplayName();
        e.printStackTrace();
        return failedReadings;
    }

    private void broadcastResult(ArrayList<RoadCamera> roadCameras) {
        Intent localIntent = new Intent(BROADCAST_READING_DONE);
        localIntent.putExtra(ROAD_CAMERA_LIST_KEY, roadCameras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
