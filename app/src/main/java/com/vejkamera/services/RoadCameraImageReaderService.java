package com.vejkamera.services;

import android.app.IntentService;
import android.content.Context;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
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
    private Context manualContext = null;

    public RoadCameraImageReaderService() {
        super(RoadCameraImageReaderService.class.getSimpleName());
    }

    public RoadCameraImageReaderService(Context context) {
        super(RoadCameraImageReaderService.class.getSimpleName());
        manualContext = context;
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
        roadCameras = readRequest.getRequestedRoadCameras((getBaseContext() != null ? getBaseContext() : manualContext));
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
            } catch (MalformedURLException|URISyntaxException e) {
                //TODO Add image of broken camera
                failedReadings = updateFailedReading(failedReadings, roadCamera, e, thumbnailsOnly);
            } catch (IOException e) {
                //TODO Add image of broken camera
                failedReadings = updateFailedReading(failedReadings, roadCamera, e, thumbnailsOnly);
            }
        }
        if(failedReadings != null) {
            Log.d(getClass().getSimpleName(), failedReadings);
        }
        broadcastResult(roadCameras);
    }

    private void updateImageFromURL(RoadCamera roadCamera, boolean thumbnail) throws IOException, URISyntaxException {
        String urlLink = thumbnail ? roadCamera.getThumbnailLink() : roadCamera.getImageLink();
        if(urlLink.startsWith("http:")) {
            URI uri = new URI(urlLink);
            URL url = new URL(uri.toASCIIString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            if(thumbnail) {
                roadCamera.setThumbnail(BitmapFactory.decodeStream(is));
                roadCamera.setThumbnailReadingFailed(false);
            } else {
                roadCamera.setBitmap(BitmapFactory.decodeStream(is));
                roadCamera.setBitmapReadingFailed(false);
            }
        }
    }

    private String updateFailedReading(String failedReadings, RoadCamera roadCamera, Exception e, boolean thumbnail) {
        if(thumbnail) {
            roadCamera.setThumbnailReadingFailed(true);
        } else {
            roadCamera.setBitmapReadingFailed(true);
        }
        failedReadings = (failedReadings != null ? failedReadings + ", " : "\n") + roadCamera.getTitle() + " (Thumbnail: " + roadCamera.getThumbnailLink() + ", " + ", URL: " + roadCamera.getImageLink() + ")\n";
        e.printStackTrace();
        Log.d(getClass().getSimpleName(), "Failed to read camera " + roadCamera.getTitle(), e);
        return failedReadings;
    }

    private void broadcastResult(List<RoadCamera> roadCameras) {
        Intent localIntent = new Intent(BROADCAST_IMAGE_READING_DONE);
        localIntent.putExtra(READ_REQUEST_KEY, readRequest);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
