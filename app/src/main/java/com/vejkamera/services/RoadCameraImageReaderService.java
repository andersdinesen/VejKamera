package com.vejkamera.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import com.vejkamera.RoadCamera;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Anders on 27-05-2015.
 */
public class RoadCameraImageReaderService extends IntentService {
    public static final String BROADCAST_IMAGE_READING_DONE = "com.vejkamera.IMAGE_READING_DONE";
    public static final String ROAD_CAMERA_LIST_KEY = "ROAD_CAMERA_LIST";
    public static final String AREA_CAMERA_ID_KEY = "AREA_CAMERAS";
    public static final String THUMBNAILS_ONLY_KEY = "THUMBNAILS_ONLY";
    public static final String BROADCAST_RECEIVER_KEY = "BROADCAST_RECEIVER";
    private static ArrayList<RoadCamera> allRoadCameras = null;
    private static Boolean listReadingCompleted = false;

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

    @Override
    protected void onHandleIntent(Intent intent) {
        //String urlPath = getString(R.string.URL_path);
        ArrayList<RoadCamera> roadCameras = getListOfCameras(intent);
        String failedReadings = null;
        boolean thumbnailsOnly = (intent.hasExtra(THUMBNAILS_ONLY_KEY) && intent.getStringExtra(THUMBNAILS_ONLY_KEY).equalsIgnoreCase("Y"));

        for (RoadCamera roadCamera : roadCameras) {
            try {
                URL url = new URL(thumbnailsOnly ? roadCamera.getThumbnailLink() : roadCamera.getImageLink());
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

    private ArrayList<RoadCamera> getListOfCameras(Intent intent){
        if(intent.hasExtra(ROAD_CAMERA_LIST_KEY)) {
            return intent.getParcelableArrayListExtra(ROAD_CAMERA_LIST_KEY);
        } else {
            return getCamerasByArea(intent);
        }
    }

    private ArrayList<RoadCamera> getCamerasByArea(Intent intent){
        // TODO: Timeout for reading the thumbnails again
        if(!listReadingCompleted) {
            RoadCameraListingReaderService listingReaderService = new RoadCameraListingReaderService();
            listingReaderService.onHandleIntent(intent);
            allRoadCameras = listingReaderService.getRoadCameras();
            listReadingCompleted = true;
        }

        if(intent.hasExtra(AREA_CAMERA_ID_KEY)){
            return filterListOfCameras(intent.getIntExtra(AREA_CAMERA_ID_KEY, 0), allRoadCameras);
        } else {
            return allRoadCameras;
        }

    }

    private String updateFailedReading(String failedReadings, RoadCamera roadCamera, Exception e) {
        failedReadings = (failedReadings != null ? failedReadings + ", " : "") + roadCamera.getTitle();
        e.printStackTrace();
        return failedReadings;
    }

    private void broadcastResult(ArrayList<RoadCamera> roadCameras) {
        Intent localIntent = new Intent(BROADCAST_IMAGE_READING_DONE);
        localIntent.putExtra(ROAD_CAMERA_LIST_KEY, roadCameras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private ArrayList<RoadCamera> filterListOfCameras(int areaResourceId, ArrayList<RoadCamera> roadCameras){
        Polygon areaPolygon = getAreaPolygon(areaResourceId);
        return findRoadCameraInPolygon(roadCameras, areaPolygon);
    }

    @NonNull
    private ArrayList<RoadCamera> findRoadCameraInPolygon(ArrayList<RoadCamera> roadCameras, Polygon areaPolygon) {
        ArrayList<RoadCamera> resultCameras = new ArrayList<>();
        Iterator<RoadCamera> roadCameraIterator = roadCameras.iterator();
        while (roadCameraIterator.hasNext()){
            RoadCamera currentRoadCamera = roadCameraIterator.next();
            float x = currentRoadCamera.getLongitude().floatValue();
            float y = currentRoadCamera.getLatitude().floatValue();
            Point currentPoint = new Point(x,y);

            if(areaPolygon.contains(currentPoint)) {
                resultCameras.add(currentRoadCamera);
            }
        }
        return resultCameras;
    }

    private Polygon getAreaPolygon(int areaResourceId) {
        String[] coordinatesStrings = getResources().getStringArray(Constants.AREA_COORDINATES.get(areaResourceId));
        Polygon.Builder polygonBuilder = Polygon.Builder();

        for(int i=0; coordinatesStrings != null && i<coordinatesStrings.length; i++){
            if(coordinatesStrings[i] != null){
                String[] pointArray = coordinatesStrings[i].split(",");
                float x = Float.valueOf(pointArray[1]);
                float y = Float.valueOf(pointArray[0]);
                polygonBuilder.addVertex(new Point(x, y));
            }
        }
        polygonBuilder.close();

        // Find cities that should be cutout
        Integer[] areaCutoutsResourceIds = Constants.AREA_CUTOUTS.get(areaResourceId);
        for(int i=0; areaCutoutsResourceIds != null && i<areaCutoutsResourceIds.length; i++){
            String[] coordinatesCutoutStrings = getResources().getStringArray(Constants.AREA_COORDINATES.get(areaCutoutsResourceIds[i]));
            for(int j=0; coordinatesCutoutStrings != null && coordinatesCutoutStrings.length>j; j++){
                String[] pointArray = coordinatesCutoutStrings[j].split(",");
                float x = Float.valueOf(pointArray[1]);
                float y = Float.valueOf(pointArray[0]);
                polygonBuilder.addVertex(new Point(x, y));
            }
            polygonBuilder.close();
        }

        return polygonBuilder.build();
    }
}
