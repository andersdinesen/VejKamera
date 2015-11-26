package com.vejkamera.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vejkamera.Constants;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;
import com.vejkamera.favorites.RoadCameraArchiveHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Anders on 27-05-2015.
 */
public class RoadCameraImageReaderService extends IntentService {
    public static final String BROADCAST_IMAGE_READING_DONE = "com.vejkamera.IMAGE_READING_DONE";
    public static final String READ_REQUEST_KEY = "READ_REQUEST";

    private List<RoadCamera> roadCameras = null;
    private RoadCameraReadRequest readRequest = null;

    public RoadCameraImageReaderService() {
        super(RoadCameraImageReaderService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RoadCameraImageReaderService(String name) {
        super(name);
    }

    public List<RoadCamera> getRoadCameras()  {
        return roadCameras;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //String urlPath = getString(R.string.URL_path);
        readRequest = intent.getParcelableExtra(READ_REQUEST_KEY);
        roadCameras = readRequest.getRequestedRoadCameras(getBaseContext());
        String failedReadings = null;
        boolean thumbnailsOnly = readRequest.isThumbNailsOnly();

        for (RoadCamera roadCamera : roadCameras) {
            try {
                //TODO Timeout for reading thumbnails again
                if(thumbnailsOnly && roadCamera.getThumbnail() == null) {
                    updateImageFromURL(roadCamera, true);
                } else if(!thumbnailsOnly) {
                    updateImageFromURL(roadCamera, false);
                }
            } catch (MalformedURLException e) {
                //TODO Add image of broken camera
                failedReadings = updateFailedReading(failedReadings, roadCamera, e);
            } catch (IOException e) {
                //TODO Add image of broken camera
                failedReadings = updateFailedReading(failedReadings, roadCamera, e);
            }
        }
        if(failedReadings != null) {
            Log.d(getClass().getSimpleName(), failedReadings);
        }
        broadcastResult(roadCameras);
    }

    private void updateImageFromURL(RoadCamera roadCamera, boolean thumbnail) throws IOException {
        String urlLink = thumbnail ? roadCamera.getThumbnailLink() : roadCamera.getImageLink();
        if(urlLink.startsWith("http:")) {
            URL url = new URL(urlLink);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            if(thumbnail) {
                roadCamera.setThumbnail(BitmapFactory.decodeStream(is));
            } else {
                roadCamera.setBitmap(BitmapFactory.decodeStream(is));

            }
        }
    }

    private String updateFailedReading(String failedReadings, RoadCamera roadCamera, Exception e) {
        failedReadings = (failedReadings != null ? failedReadings + ", " : "\n") + roadCamera.getTitle() + " (Thumbnail: " + roadCamera.getThumbnailLink() + ", " + ", URL: " + roadCamera.getImageLink() + ")\n";
        e.printStackTrace();
        return failedReadings;
    }

    private void broadcastResult(List<RoadCamera> roadCameras) {
        Intent localIntent = new Intent(BROADCAST_IMAGE_READING_DONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
