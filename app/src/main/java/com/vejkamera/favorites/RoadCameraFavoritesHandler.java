package com.vejkamera.favorites;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by Anders on 05-08-2015.
 */
public final class RoadCameraFavoritesHandler {
    private final static String SHARED_PREF_NAME = "VEJ_KAMERA";
    private final static String FAVORITE_SYNC_IDS_PREF_NAME = "FAVORITE_SYNC_IDS";
    private final static String FAVORITE_IMAGE_LINK_PREF_NAME = "FAVORITE_IMAGE_LINK_";
    private final static String FAVORITE_TITLE_PREF_NAME = "FAVORITE_TITLE_";
    private final static String FAVORITE_INFO_PREF_NAME = "FAVORITE_INFO_";
    private final static String FAVORITE_LATITUDE_PREF_NAME = "FAVORITE_LATITUDE_";
    private final static String FAVORITE_LONGITUDE_PREF_NAME = "FAVORITE_LONGITUDE_";

    public static void addFavorite(RoadCamera roadCamera, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Set<String> existingSyncIds = sharedPref.getStringSet(FAVORITE_SYNC_IDS_PREF_NAME, new HashSet<String>());

        int oldSize = existingSyncIds.size();
        existingSyncIds.add(roadCamera.getSyncId());

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(FAVORITE_SYNC_IDS_PREF_NAME, existingSyncIds);

        //Was the sync id added or already existing?
        if(oldSize<existingSyncIds.size()){
            editor.putString(FAVORITE_IMAGE_LINK_PREF_NAME + roadCamera.getSyncId(), roadCamera.getImageLink());
            editor.putString(FAVORITE_TITLE_PREF_NAME + roadCamera.getSyncId(), roadCamera.getTitle());
            editor.putString(FAVORITE_INFO_PREF_NAME + roadCamera.getSyncId(), roadCamera.getInfo());
            editor.putString(FAVORITE_LATITUDE_PREF_NAME + roadCamera.getSyncId(), Double.toString(roadCamera.getLatitude()));
            editor.putString(FAVORITE_LONGITUDE_PREF_NAME + roadCamera.getSyncId(), Double.toString(roadCamera.getLongitude()));
        }

        editor.commit();
        Toast.makeText(context, R.string.toast_favorite_added, Toast.LENGTH_SHORT).show();
    }

    public static ArrayList<RoadCamera> getFavorites(Context context) {
        ArrayList<RoadCamera> favorites = new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Set<String> existingSyncIds = sharedPref.getStringSet(FAVORITE_SYNC_IDS_PREF_NAME, new HashSet<String>());

        Iterator<String> syncIdsIterator = existingSyncIds.iterator();
        while (syncIdsIterator.hasNext()){
            RoadCamera favoriteCamera = new RoadCamera();
            String favoriteSyncId = syncIdsIterator.next();
            favoriteCamera.setSyncId(favoriteSyncId);
            favoriteCamera.setImageLink(sharedPref.getString(FAVORITE_IMAGE_LINK_PREF_NAME + favoriteSyncId, null));
            favoriteCamera.setTitle(sharedPref.getString(FAVORITE_TITLE_PREF_NAME + favoriteSyncId, null));
            favoriteCamera.setInfo(sharedPref.getString(FAVORITE_INFO_PREF_NAME + favoriteSyncId, null));
            favoriteCamera.setLatitude(Double.valueOf(sharedPref.getString(FAVORITE_LATITUDE_PREF_NAME + favoriteSyncId, null)));
            favoriteCamera.setLongitude(Double.valueOf(sharedPref.getString(FAVORITE_LONGITUDE_PREF_NAME + favoriteSyncId, null)));
            favorites.add(favoriteCamera);
        }

        return favorites;
    }

    public static boolean isFavorite(RoadCamera roadCamera, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Set<String> existingSyncIds = sharedPref.getStringSet(FAVORITE_SYNC_IDS_PREF_NAME, new HashSet<String>());

        return existingSyncIds.contains(roadCamera.getSyncId());
    }

    public static void removeFavorite(RoadCamera roadCamera, Context context) {
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
}
