package com.vejkamera.services;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.vejkamera.RoadCamera;
import com.vejkamera.favorites.RoadCameraArchiveHandler;

import java.util.ArrayList;

/**
 * Created by ad on 12-11-2015.
 */
public class RoadCameraReadRequest implements Parcelable {
    public final static int READ_TYPE_FAVORITES = 0;
    public final static int READ_TYPE_ALL = 1;
    public final static int READ_TYPE_SYNC_IDS = 2;
    public final static int READ_TYPE_AREA = 3;
    private int readType = 0;
    private ArrayList<String> syncIds = new ArrayList<>();
    private boolean thumbNailsOnly = false;

    public RoadCameraReadRequest(int readType) {
        this.readType = readType;
    }

    public RoadCameraReadRequest(int readType, String syncId) {
        this.readType = readType;
        this.syncIds.add(syncId);
    }

    public RoadCameraReadRequest(int readType, ArrayList<String> syncIds) {
        this.readType = readType;
        this.syncIds = syncIds;
    }

    protected RoadCameraReadRequest(Parcel in) {
        readType = in.readInt();
        syncIds = in.createStringArrayList();
        thumbNailsOnly = in.readByte() != 0;
    }

    public void addSyncId(String syncId) {
        syncIds.add(syncId);
    }

    public void setSyncIds(ArrayList<String> syncIds){
        this.syncIds = syncIds;
    }

    public boolean isThumbNailsOnly() { return thumbNailsOnly; }

    public void setThumbNailsOnly(boolean thumbNailsOnly) { this.thumbNailsOnly = thumbNailsOnly; }

    public ArrayList<RoadCamera> getRequestedRoadCameras(Context context){
        if(readType == READ_TYPE_FAVORITES){
            return RoadCameraArchiveHandler.getFavorites(context);
        } else if(readType == READ_TYPE_ALL) {
            return RoadCameraArchiveHandler.getAllRoadCameras(context);
        } else if(readType == READ_TYPE_SYNC_IDS) {
            return RoadCameraArchiveHandler.getRoadCameraFromSyncIdList(syncIds, context);
        }

        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(readType);
        dest.writeStringList(syncIds);
        dest.writeByte((byte) (thumbNailsOnly ? 1 : 0));
    }

    public static final Creator<RoadCameraReadRequest> CREATOR = new Creator<RoadCameraReadRequest>() {
        @Override
        public RoadCameraReadRequest createFromParcel(Parcel in) {
            return new RoadCameraReadRequest(in);
        }

        @Override
        public RoadCameraReadRequest[] newArray(int size) {
            return new RoadCameraReadRequest[size];
        }
    };
}
