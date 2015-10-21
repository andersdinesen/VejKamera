package com.vejkamera.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;

import java.util.ArrayList;

/**
 * Created by ad on 21-10-2015.
 */
public class RoadCameraLoopReaderService extends IntentService{
    public static final String BROADCAST_IMAGE_LOOP_READING_UPDATE = "com.vejkamera.IMAGE_LOOP_READING_UPDATE";
    private  ArrayList<RoadCamera> roadCameras = new ArrayList<>();

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
        prepareReceiver();
        readCameraList();
    }

    private void prepareReceiver(){
        // Prepare for receiving the result when the favorites are read
        LoopResponseReceiver favoritesResponseReceiver = new LoopResponseReceiver();
        IntentFilter intentFilter = new IntentFilter(RoadCameraImageReaderService.BROADCAST_IMAGE_READING_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(favoritesResponseReceiver, intentFilter);
        System.out.println("Receiver prepared");
    }

    private void readCameraList(){
        //Start service to road cameras
        Intent readIntent = new Intent(this, RoadCameraImageReaderService.class);
        readIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, roadCameras);
        startService(readIntent);
        System.out.println("Started RoadCameraImageReaderService");
    }

    private class LoopResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            roadCameras.clear();

            ArrayList<RoadCamera> updatedFavorites = intent.getParcelableArrayListExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY);
            roadCameras.addAll(updatedFavorites);
            System.out.println("Received a result: " + roadCameras.size());
            broadcastResult();
            System.out.println("Broadcasting done. Sleeping");
            /*
            try {
                Thread.sleep(R.integer.camera_image_update_interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            handlePause(20000);
            System.out.println("Done sleeping");
            //readCameraList();
        }
    }

    private void broadcastResult() {
        Intent localIntent = new Intent(BROADCAST_IMAGE_LOOP_READING_UPDATE);
        localIntent.putExtra(RoadCameraImageReaderService.ROAD_CAMERA_LIST_KEY, roadCameras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void handlePause(int pauseTime){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                readCameraList();
            }
        }, pauseTime);
    }
}
