package com.vejkamera.favorites.comparators;

import android.location.Location;

import com.vejkamera.RoadCamera;

import java.util.Comparator;
import java.util.List;

public class RoadCameraOriginalSortingComparator implements Comparator<RoadCamera> {
    List<String> originalSorting;

    public RoadCameraOriginalSortingComparator(List<String> originalSorting){
        this.originalSorting = originalSorting;
    }

    @Override
    public int compare(RoadCamera lhsRoadCamera, RoadCamera rhsRoadCamera) {
        if(lhsRoadCamera == null){
            return -1;
        }

        if(rhsRoadCamera == null){
            return 1;
        }


        for (String currentSyncId : originalSorting){
            if (lhsRoadCamera.getSyncId().equals(currentSyncId)){
                return -1;
            }
            if (rhsRoadCamera.getSyncId().equals(currentSyncId)){
                return 1;
            }
        }
        return 0;
    }
}
