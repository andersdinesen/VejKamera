package com.vejkamera.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.vejkamera.RoadCamera;
import com.vejkamera.favorites.RoadCameraArchiveHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Anders on 27-05-2015.
 */
public class RoadCameraListingReaderService extends IntentService {
    public static final String BROADCAST_LIST_READING_DONE = "com.vejkamera.LIST_READING_DONE";
    public static final String ROAD_CAMERA_LIST_KEY = "ROAD_CAMERA_LIST";
    private ArrayList<RoadCamera> roadCameras = null;

    public RoadCameraListingReaderService() {
        super(RoadCameraListingReaderService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RoadCameraListingReaderService(String name) {
        super(name);
    }

    public ArrayList<RoadCamera> getRoadCameras(){
        return roadCameras;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String rawReading = getReadJsonObjectsFromNetwork();

        roadCameras = convertJSONIntoRoadCamList(rawReading);

        updateFavoritesInArchive(roadCameras);
        broadcastResult(roadCameras);
    }


    private String getReadJsonObjectsFromNetwork() {
        URL url;
        InputStream inputStream;
        StringBuilder stringBuilder;
        String result = null;
        try {
            url = new URL("http://prod.middleman.dk/api/webcam/json/synced?type=webCam&syncVersion=1&");
            HttpURLConnection connection = null;
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-VD-APPVERSION", "500");
            connection.setRequestProperty("X-VD-APPID", "ANDROID");
            connection.connect();
            inputStream = connection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            result = stringBuilder.toString();
        } catch (IOException e) {
            //TODO: Add network exception handling
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<RoadCamera> convertJSONIntoRoadCamList(String rawReading) {
        ArrayList<RoadCamera> roadCameraList = new ArrayList<>();
        RoadCamera roadCamera = null;
        try {
            JSONObject roadCameraListing = new JSONObject(rawReading);
            JSONObject entities = roadCameraListing.getJSONObject("entities");
            JSONArray jsonRoadCameraList = entities.getJSONArray("neworchanged");
            for(int i=0; i<jsonRoadCameraList.length(); i++){
                JSONObject jsonCamera = (JSONObject) jsonRoadCameraList.get(i);
                roadCamera = new RoadCamera();
                roadCamera.setImageLink(jsonCamera.optString("link"));
                roadCamera.setLongitude(jsonCamera.optDouble("longitude"));
                roadCamera.setLatitude(jsonCamera.optDouble("latitude"));
                roadCamera.setState(jsonCamera.optString("state"));
                roadCamera.setInfo(jsonCamera.optString("info"));
                roadCamera.setTitle(jsonCamera.optString("title"));
                roadCamera.setDirection(jsonCamera.optInt("direction"));
                roadCamera.setTime(jsonCamera.optLong("time"));
                roadCamera.setSyncId(jsonCamera.optString("syncId"));
                roadCamera.setThumbnailLink(jsonCamera.optString("thumbnailLink"));
                roadCameraList.add(roadCamera);
            }

        } catch (JSONException e) {
            //TODO: Handle error on reading JSON content
            e.printStackTrace();
        }
        return roadCameraList;
    }

    private void updateFavoritesInArchive(ArrayList<RoadCamera> roadCameras){
        RoadCameraArchiveHandler.setAllRoadCameras(roadCameras);
    }

    private void broadcastResult(ArrayList<RoadCamera> roadCameras) {
        Intent localIntent = new Intent(BROADCAST_LIST_READING_DONE);
        localIntent.putExtra(ROAD_CAMERA_LIST_KEY, roadCameras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
