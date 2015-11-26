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
    public static final String ROAD_CAMERA_LIST_KEY = "ROAD_CAMERA_LIST";
    public static final String READ_REQUEST_KEY = "READ_REQUEST";
    public static final String TYPE_TO_READ_KEY = "TYPE_TO_READ";
    public static final String TYPE_TO_READ_FAVORITES = "FAVORITES";
    public static final String TYPE_TO_READ_SYNC_ID = "SYNC_ID";
    public static final String VALUES_TO_READ = "VALUES_TO_READ";
    public static final String AREA_CAMERA_ID_KEY = "AREA_CAMERAS";
    public static final String THUMBNAILS_ONLY_KEY = "THUMBNAILS_ONLY";
    private static ArrayList<RoadCamera> allRoadCameras = null;
    private static Boolean listReadingCompleted = false;

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
        roadCameras = getListOfCameras(intent);
        String failedReadings = null;
        boolean thumbnailsOnly = readRequest.isThumbNailsOnly();//( intent.hasExtra(THUMBNAILS_ONLY_KEY) && intent.getStringExtra(THUMBNAILS_ONLY_KEY).equalsIgnoreCase("Y"));

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

    private List<RoadCamera> getListOfCameras(Intent intent){
        if(readRequest != null){
            return readRequest.getRequestedRoadCameras(getBaseContext());
        }

        if(intent.hasExtra(ROAD_CAMERA_LIST_KEY)) {
            return intent.getParcelableArrayListExtra(ROAD_CAMERA_LIST_KEY);
        } else {
            return null;//getCamerasByArea(intent);
        }
    }
/*
    private ArrayList<RoadCamera> getCamerasByArea(Intent intent){
        // TODO: Timeout for reading the thumbnails again
        if(!listReadingCompleted) {
            RoadCameraListingReaderService listingReaderService = new RoadCameraListingReaderService();
            listingReaderService.onHandleIntent(intent);
            allRoadCameras = listingReaderService.getRoadCameras();
            listReadingCompleted = true;
        }

        if(intent.hasExtra(AREA_CAMERA_ID_KEY) && intent.getIntExtra(AREA_CAMERA_ID_KEY, 0) != R.string.all_areas){
            return RoadCameraArchiveHandler.filterListOfCameras(intent.getIntExtra(AREA_CAMERA_ID_KEY, 0), allRoadCameras, this);
        } else {
            return allRoadCameras;
        }
    }
*/

    private String updateFailedReading(String failedReadings, RoadCamera roadCamera, Exception e) {
        failedReadings = (failedReadings != null ? failedReadings + ", " : "\n") + roadCamera.getTitle() + " (Thumbnail: " + roadCamera.getThumbnailLink() + ", " + ", URL: " + roadCamera.getImageLink() + ")\n";
        e.printStackTrace();
        return failedReadings;
    }

    private void broadcastResult(List<RoadCamera> roadCameras) {
        Intent localIntent = new Intent(BROADCAST_IMAGE_READING_DONE);
        //localIntent.putExtra(ROAD_CAMERA_LIST_KEY, roadCameras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
