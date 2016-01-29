package com.vejkamera.map;

import android.content.Context;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;

/**
 * Created by ad on 14-01-2016.
 */
public class DirectionToMapPin {

    public static int getMapPinIconFromRoadCamera(RoadCamera roadCamera, Context context){
        if (roadCamera.getDirection()>337.5 || (roadCamera.getDirection()>0 && roadCamera.getDirection()<22.5) ){
            return R.drawable.app_icon_map_pin_000;
        } else if (roadCamera.getDirection() < 67.5){
            return R.drawable.app_icon_map_pin_045;
        } else if (roadCamera.getDirection() < 112.5){
            return R.drawable.app_icon_map_pin_090;
        } else if (roadCamera.getDirection() < 157.5){
            return R.drawable.app_icon_map_pin_135;
        } else if (roadCamera.getDirection() < 202.5){
            return R.drawable.app_icon_map_pin_180;
        } else if (roadCamera.getDirection() < 247.5){
            return R.drawable.app_icon_map_pin_225;
        } else if (roadCamera.getDirection() < 292.5){
            return R.drawable.app_icon_map_pin_270;
        } else if (roadCamera.getDirection() < 337.5){
            return R.drawable.app_icon_map_pin_315;
        } else if (roadCamera.getDirection() == -1){
            return getCameraDirectionFromInfo(roadCamera, context);
        } else {
            return R.drawable.app_icon_map_pin_090;
        }
    }

    protected static int getCameraDirectionFromInfo(RoadCamera roadCamera, Context context){
        if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.northeast_info_search))){
            return R.drawable.app_icon_map_pin_045;
        } else if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.southheast_info_search))){
            return R.drawable.app_icon_map_pin_135;
        } else if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.southwest_info_search))){
            return R.drawable.app_icon_map_pin_225;
        } else if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.northwest_info_search))){
            return R.drawable.app_icon_map_pin_315;
        } else if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.north_info_search))){
            return R.drawable.app_icon_map_pin_000;
        } else if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.east_info_search))){
            return R.drawable.app_icon_map_pin_090;
        } else if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.south_info_search))){
            return R.drawable.app_icon_map_pin_180;
        } else if(roadCamera.getInfo().toUpperCase().contains(context.getString(R.string.west_info_search))){
            return R.drawable.app_icon_map_pin_270;
        } else {
            return R.drawable.app_icon_map_pin_090;
        }
    }

}
