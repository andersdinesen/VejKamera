package com.vejkamera.favorites;

import android.content.Context;
import android.content.SharedPreferences;

import com.vejkamera.R;

import java.util.ArrayList;

/**
 * Created by ad on 02-12-2015.
 */
public class RoadCameraProfileHandler {
    private final static String AVAILABLE_PROFILE_IDS_PREF_NAME = "AVALIABLE_PROFILES";
    private final static String PROFILES_NAMES_PREF_NAME = "PROFILE_NAMES";
    private final static String CURRENT_PROFILE_PREF_NAME = "CURRENT_PROFILE";
    private final static String DEFAULT_PROFILE_NAME = "Profile";

    public static Integer getCurrentProfileId(Context context){
         SharedPreferences sharedPref = context.getSharedPreferences(RoadCameraArchiveHandler.SHARED_PREF_NAME, Context.MODE_PRIVATE);
         int currentProfileId = sharedPref.getInt(CURRENT_PROFILE_PREF_NAME, -1);
         return (currentProfileId != -1 ? currentProfileId : null);
    }

    public static ArrayList<Integer> getAllProfileIds(Context context){
        ArrayList<Integer> result = new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences(RoadCameraArchiveHandler.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if(!sharedPref.contains(AVAILABLE_PROFILE_IDS_PREF_NAME)){
            createNewProfile(context.getString(R.string.default_profile_name), context);
        }
        String[] availableProfilesArray = sharedPref.getString(AVAILABLE_PROFILE_IDS_PREF_NAME, "").split(",");
        for (int i = 0; i < availableProfilesArray.length; i++) {
            result.add(Integer.valueOf(availableProfilesArray[i]));
        }
        return result;
    }

    public static String getProfileName(int profileId, Context context){
        //int currentProfileId = getCurrentProfileId(context);
        SharedPreferences sharedPref = context.getSharedPreferences(RoadCameraArchiveHandler.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(PROFILES_NAMES_PREF_NAME + "_" + profileId, DEFAULT_PROFILE_NAME + " " + profileId);
    }

    public static void setCurrentProfileName(String newName, Context context){
        int currentProfileId = getCurrentProfileId(context);
        SharedPreferences sharedPref = context.getSharedPreferences(RoadCameraArchiveHandler.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PROFILES_NAMES_PREF_NAME + "_" + currentProfileId, newName);
        editor.commit();
    }

    public static void changeCurrentProfile(int newProfile, Context context){
        //favoriteRoadCameras.clear();
        SharedPreferences sharedPref = context.getSharedPreferences(RoadCameraArchiveHandler.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CURRENT_PROFILE_PREF_NAME, newProfile);
        editor.commit();

        RoadCameraArchiveHandler.clearCachedFavorites(context);
    }

    public static int createNewProfile(String name, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(RoadCameraArchiveHandler.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String availableProfileIds = sharedPref.getString(AVAILABLE_PROFILE_IDS_PREF_NAME, null);

        int currentMaxId = -1;
        if(availableProfileIds!=null) {
            for (int currentId : getAllProfileIds(context)) {
                currentMaxId = (currentId > currentMaxId ? currentId : currentMaxId);
            }
        }
        int newProfileId = currentMaxId + 1;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AVAILABLE_PROFILE_IDS_PREF_NAME, (availableProfileIds != null ? availableProfileIds + "," : "" ) + newProfileId);
        editor.putString(PROFILES_NAMES_PREF_NAME + "_" + newProfileId, name);
        editor.commit();

        return newProfileId;
    }

    public static void removeProfile(int profileId, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(RoadCameraArchiveHandler.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String newListOfProfiles = "";
        ArrayList<Integer> allProfileIds = getAllProfileIds(context);
        Integer previousId = null;
        for(int i = 0; i< allProfileIds.size() ; i++  ){
            Integer currentId = allProfileIds.get(i);
            if(currentId != profileId){
                newListOfProfiles = newListOfProfiles + (i!=0 ? "," : "") + currentId ;
                previousId = (previousId != null ? previousId : currentId);
            }
        }

        if(previousId != null) {
            changeCurrentProfile(previousId, context);
        } else {
            editor.remove(CURRENT_PROFILE_PREF_NAME);
        }

        editor.putString(AVAILABLE_PROFILE_IDS_PREF_NAME, newListOfProfiles);
        editor.remove(PROFILES_NAMES_PREF_NAME + "_" + profileId);

        editor.commit();
    }




}
