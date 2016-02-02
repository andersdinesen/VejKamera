package com.vejkamera.favorites.adapter;

import android.location.Location;

import com.vejkamera.RoadCamera;

import java.util.Comparator;

public class RoadCameraTitleComparator implements Comparator<RoadCamera> {
    Location currentLocation;


    @Override
    public int compare(RoadCamera lhsRoadCamera, RoadCamera rhsRoadCamera) {
        if(lhsRoadCamera == null){
            return -1;
        }

        if(rhsRoadCamera == null){
            return 1;
        }

        return lhsRoadCamera.getTitle().compareTo(rhsRoadCamera.getTitle());
    }
}
