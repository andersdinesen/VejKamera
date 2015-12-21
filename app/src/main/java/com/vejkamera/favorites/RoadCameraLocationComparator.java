package com.vejkamera.favorites;

import android.location.Location;

import com.vejkamera.RoadCamera;

import java.util.Comparator;

public class RoadCameraLocationComparator implements Comparator<RoadCamera> {
    Location currentLocation;

    public RoadCameraLocationComparator(Location currentLocation){
        this.currentLocation = currentLocation;
    }

    @Override
    public int compare(RoadCamera lhsRoadCamera, RoadCamera rhsRoadCamera) {
        if(lhsRoadCamera == null){
            return -1;
        }

        if(rhsRoadCamera == null){
            return 1;
        }

        Location lhsLocation = new Location("lhs");
        lhsLocation.setLatitude(lhsRoadCamera.getLatitude());
        lhsLocation.setLongitude(lhsRoadCamera.getLongitude());
        float distanceToLhs = currentLocation.distanceTo(lhsLocation);

        Location rhsLocation = new Location("rhs");
        rhsLocation.setLatitude(rhsRoadCamera.getLatitude());
        rhsLocation.setLongitude(rhsRoadCamera.getLongitude());
        float distanceToRhs = currentLocation.distanceTo(rhsLocation);

        return (distanceToLhs < distanceToRhs ? 1 : -1);
    }
}
