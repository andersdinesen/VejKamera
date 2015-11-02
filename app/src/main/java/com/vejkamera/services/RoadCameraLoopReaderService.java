package com.vejkamera.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;

import java.util.ArrayList;

/**
 * Created by ad on 21-10-2015.
 */
public class RoadCameraLoopReaderService extends IntentService{
    public static final String BROADCAST_IMAGE_LOOP_READING_UPDATE = "com.vejkamera.IMAGE_LOOP_READING_UPDATE";
    public static final String BROADCAST_IMAGE_LOOP_READING_STOP = "com.vejkamera.IMAGE_LOOP_READING_STOP";
    private ArrayList<RoadCamera> roadCameras = new ArrayList<>();
    private RoadCameraImageReaderService roadCameraImageReaderService = new RoadCameraImageReaderService();
    private Boolean continueLoop = true;
    private LoopFavoritesResponseReceiver loopFavoritesResponseReceiver = new LoopFavoritesResponseReceiver();

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
        setupReceiver();
        readCameraListInLoop();
    }

    private void readCameraListInLoop() {
        while (continueLoop) {
            Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);
            readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, roadCameras);
            // Not starting the service a a separate thread, since we are already in separate (from main) thread
            roadCameraImageReaderService.onHandleIntent(readIntent);
            roadCameras = roadCameraImageReaderService.getRoadCameras();
            broadcastResult();
            sleep();
        }
    }

    private void setupReceiver(){
        IntentFilter intentFilter = new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(loopFavoritesResponseReceiver, intentFilter);

        IntentFilter intentStopFilter = new IntentFilter(BROADCAST_IMAGE_LOOP_READING_STOP);
        LocalBroadcastManager.getInstance(this).registerReceiver(new LoopStopResponseReceiver(), intentStopFilter);
    }

    private void sleep(){
        synchronized (this) {
            try {
                if(continueLoop) {
                    wait(getResources().getInteger(R.integer.camera_image_update_interval));
                }
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        continueLoop = false;
        synchronized (this){
            this.notify();
        }
    }

    private void broadcastResult() {
        Intent localIntent = new Intent(BROADCAST_IMAGE_LOOP_READING_UPDATE);
        localIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, roadCameras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private class LoopFavoritesResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            roadCameras = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            broadcastResult();
        }
    }

    private class LoopStopResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onDestroy();
        }
    }

}
