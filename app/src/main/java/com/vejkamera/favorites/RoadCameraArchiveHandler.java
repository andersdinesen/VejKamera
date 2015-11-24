package com.vejkamera.favorites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;
import com.vejkamera.Constants;
import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.services.RoadCameraListingReaderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by Anders on 05-08-2015.
 */
public final class RoadCameraArchiveHandler {
    private final static String SHARED_PREF_NAME = "VEJ_KAMERA";
    private final static String FAVORITE_SYNC_IDS_PREF_NAME = "FAVORITE_SYNC_IDS";
    private final static String FAVORITE_IMAGE_LINK_PREF_NAME = "FAVORITE_IMAGE_LINK_";
    private final static String FAVORITE_TITLE_PREF_NAME = "FAVORITE_TITLE_";
    private final static String FAVORITE_INFO_PREF_NAME = "FAVORITE_INFO_";
    private final static String FAVORITE_LATITUDE_PREF_NAME = "FAVORITE_LATITUDE_";
    private final static String FAVORITE_LONGITUDE_PREF_NAME = "FAVORITE_LONGITUDE_";
    private final static String FAVORITES_GRID_LAYOUT_NAME = "FAVORITES_GRID_LAYOUT";
    private static ArrayList<RoadCamera> allRoadCameras = new ArrayList<>();
    private static HashMap<String, RoadCamera> allRoadCamerasHashMap = new HashMap<>();
    private static HashMap<Integer, ArrayList<RoadCamera>> areaRoadCamerasHashMap = new HashMap<>();
    private static ArrayList<RoadCamera> favoriteRoadCameras = new ArrayList<>();

    public static void addFavorite(RoadCamera roadCamera, Context context) {
        synchronized (favoriteRoadCameras) {
            favoriteRoadCameras.add(roadCamera);
            SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            Set<String> existingSyncIds = sharedPref.getStringSet(FAVORITE_SYNC_IDS_PREF_NAME, new HashSet<String>());

            int oldSize = existingSyncIds.size();
            existingSyncIds.add(roadCamera.getSyncId());

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putStringSet(FAVORITE_SYNC_IDS_PREF_NAME, existingSyncIds);

            //Was the sync id added or already existing?
            if (oldSize < existingSyncIds.size()) {
                editor.putString(FAVORITE_IMAGE_LINK_PREF_NAME + roadCamera.getSyncId(), roadCamera.getImageLink());
                editor.putString(FAVORITE_TITLE_PREF_NAME + roadCamera.getSyncId(), roadCamera.getTitle());
                editor.putString(FAVORITE_INFO_PREF_NAME + roadCamera.getSyncId(), roadCamera.getInfo());
                editor.putString(FAVORITE_LATITUDE_PREF_NAME + roadCamera.getSyncId(), Double.toString(roadCamera.getLatitude()));
                editor.putString(FAVORITE_LONGITUDE_PREF_NAME + roadCamera.getSyncId(), Double.toString(roadCamera.getLongitude()));
            }

            editor.commit();
        }
        Toast.makeText(context, R.string.toast_favorite_added, Toast.LENGTH_SHORT).show();
    }

    public static ArrayList<RoadCamera> getFavorites(Context context) {
        //ArrayList<RoadCamera> favorites = new ArrayList<>();
        synchronized (favoriteRoadCameras) {
            if (favoriteRoadCameras.size() == 0) {
                SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                Set<String> existingSyncIds = sharedPref.getStringSet(FAVORITE_SYNC_IDS_PREF_NAME, new HashSet<String>());

                Iterator<String> syncIdsIterator = existingSyncIds.iterator();
                while (syncIdsIterator.hasNext()) {
                    RoadCamera favoriteCamera = new RoadCamera();
                    String favoriteSyncId = syncIdsIterator.next();
                    favoriteCamera.setSyncId(favoriteSyncId);
                    favoriteCamera.setImageLink(sharedPref.getString(FAVORITE_IMAGE_LINK_PREF_NAME + favoriteSyncId, null));
                    favoriteCamera.setTitle(sharedPref.getString(FAVORITE_TITLE_PREF_NAME + favoriteSyncId, null));
                    favoriteCamera.setInfo(sharedPref.getString(FAVORITE_INFO_PREF_NAME + favoriteSyncId, null));
                    favoriteCamera.setLatitude(Double.valueOf(sharedPref.getString(FAVORITE_LATITUDE_PREF_NAME + favoriteSyncId, null)));
                    favoriteCamera.setLongitude(Double.valueOf(sharedPref.getString(FAVORITE_LONGITUDE_PREF_NAME + favoriteSyncId, null)));
                    favoriteRoadCameras.add(favoriteCamera);
                }
            }
        }
        return favoriteRoadCameras;
    }

    public static boolean isFavorite(RoadCamera roadCamera, Context context) {
        for(RoadCamera favoriteRoadCamera : getFavorites(context)){
            if(roadCamera.getSyncId().equalsIgnoreCase(favoriteRoadCamera.getSyncId())){
                return true;
            }
        }
        return false;
        /*
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Set<String> existingSyncIds = sharedPref.getStringSet(FAVORITE_SYNC_IDS_PREF_NAME, new HashSet<String>());
        return existingSyncIds.contains(roadCamera.getSyncId());
        */
    }

    public static void removeFavorite(RoadCamera roadCamera, Context context) {
        getFavorites(context).remove(roadCamera);

        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Set<String> existingSyncIds = sharedPref.getStringSet(FAVORITE_SYNC_IDS_PREF_NAME, new HashSet<String>());
        SharedPreferences.Editor editor = sharedPref.edit();

        String syncId = roadCamera.getSyncId();
        existingSyncIds.remove(roadCamera.getSyncId());
        editor.putStringSet(FAVORITE_SYNC_IDS_PREF_NAME, existingSyncIds);

        editor.remove(FAVORITE_TITLE_PREF_NAME + syncId);
        editor.remove(FAVORITE_IMAGE_LINK_PREF_NAME + syncId);
        editor.remove(FAVORITE_INFO_PREF_NAME + syncId);
        editor.remove(FAVORITE_LONGITUDE_PREF_NAME + syncId);
        editor.remove(FAVORITE_LATITUDE_PREF_NAME + syncId);

        editor.commit();

        Toast.makeText(context, R.string.toast_favorite_removed, Toast.LENGTH_SHORT).show();
    }

    public static void setFavoritesGridLayout(int cellsPerRow, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(FAVORITES_GRID_LAYOUT_NAME, cellsPerRow);
        editor.commit();
    }

    public static int getFavoritesGridLayout(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getInt(FAVORITES_GRID_LAYOUT_NAME, 2);
    }

    public static void initRoadCamerasArchive(Context context){
        getAllRoadCameras(context);
    }

    public static ArrayList<RoadCamera> getAllRoadCameras(Context context){
        synchronized (allRoadCameras) {
            if (allRoadCameras.size() == 0) {
                readAllCameraInfo(context);
            }
        }

        return allRoadCameras;
    }

    public static void setAllRoadCameras(ArrayList<RoadCamera> allNewRoadCameras) {
        synchronized (allRoadCameras) {
            allRoadCameras.clear();
            allRoadCamerasHashMap.clear();
            allRoadCameras.addAll(allNewRoadCameras);
            for(RoadCamera roadCamera : allNewRoadCameras){
                allRoadCamerasHashMap.put(roadCamera.getSyncId(), roadCamera);
            }
        }
    }

    private static void readAllCameraInfo(Context context) {
        Intent readIntent =  new Intent(context, RoadCameraListingReaderService.class);
        context.startService(readIntent);
    }

    public static RoadCamera getRoadCameraFromSyncId(String syncId, Context context){
        return allRoadCamerasHashMap.get(syncId);
    }

    public static ArrayList<RoadCamera> getRoadCameraFromSyncIdList(ArrayList<String> syncIdList, Context context){
        ArrayList<RoadCamera> result = new ArrayList<>();
        for(String syncId : syncIdList) {
            result.add(getRoadCameraFromSyncId(syncId, context));
        }
        return result;
    }

    public static ArrayList<RoadCamera> filterListOfCameras(int areaResourceId, Context context){
        ArrayList<RoadCamera> result = areaRoadCamerasHashMap.get(areaResourceId);

        if(result == null){
            Polygon areaPolygon = getAreaPolygon(areaResourceId, context);
            result = findRoadCameraInPolygon(getAllRoadCameras(context), areaPolygon);
            areaRoadCamerasHashMap.put(areaResourceId, result);
        }
        return result;
    }

    public static ArrayList<String> getSyncIdsFromRoadCameras(ArrayList<RoadCamera> roadCameras) {
        ArrayList<String> syncIds = new ArrayList<>();
        for(RoadCamera roadCamera : roadCameras){
            syncIds.add(roadCamera.getSyncId());
        }
        return syncIds;
    }

    @NonNull
    private static ArrayList<RoadCamera> findRoadCameraInPolygon(ArrayList<RoadCamera> roadCameras, Polygon areaPolygon) {
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

    private static Polygon getAreaPolygon(int areaResourceId, Context context) {
        String[] coordinatesStrings = context.getResources().getStringArray(Constants.AREA_COORDINATES.get(areaResourceId));
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
            String[] coordinatesCutoutStrings = context.getResources().getStringArray(Constants.AREA_COORDINATES.get(areaCutoutsResourceIds[i]));
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