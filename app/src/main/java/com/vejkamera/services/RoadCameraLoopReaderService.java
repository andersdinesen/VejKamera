package com.vejkamera.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;

import java.util.ArrayList;

/**
 * Created by ad on 21-10-2015.
 */
public class RoadCameraLoopReaderService extends IntentService{
    public static final String BROADCAST_IMAGE_LOOP_READING_UPDATE = "com.vejkamera.IMAGE_LOOP_READING_UPDATE";
    private ArrayList<RoadCamera> roadCameras = new ArrayList<>();
    private RoadCameraImageReaderService roadCameraImageReaderService = new RoadCameraImageReaderService();

    public RoadCameraLoopReaderService() {
        super(RoadCameraLoopReaderService.class.getName());
    }

    public RoadCameraLoopReaderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("Started RoadCameraLoopReaderService");
        if(!intent.hasExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY)){
            throw new IllegalArgumentException("Intent missing Extra value for " + RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
        }
        roadCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
        readCameraList();
    }

    private void readCameraList() {
        while (true) {
            Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);
            readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, roadCameras);
            // Not starting the service a a separate thread, since we are already in separate (from main) thread
            roadCameraImageReaderService.onHandleIntent(readIntent);
            roadCameras = roadCameraImageReaderService.getRoadCameras();
            broadcastResult();
            sleep();
        }
    }

    private void sleep(){
        try {
            Thread.sleep(getResources().getInteger(R.integer.camera_image_update_interval));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastResult() {
        Intent localIntent = new Intent(BROADCAST_IMAGE_LOOP_READING_UPDATE);
        localIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, roadCameras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
