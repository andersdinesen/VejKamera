package com.vejkamera;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ad on 27-05-2015.
 */
public class RoadCamera implements Parcelable{
    private String displayName = null;
    private String remoteFileName = null;
    private Bitmap bitmap = null;

    public RoadCamera() {
    }

    public RoadCamera(String displayName, String remoteFileName) {
        this.displayName = displayName;
        this.remoteFileName = remoteFileName;
    }

    public RoadCamera(String displayName, String remoteFileName, Bitmap bitmap) {
        this.displayName = displayName;
        this.remoteFileName = remoteFileName;
        this.bitmap = bitmap;
    }

    public RoadCamera(Parcel in){
        displayName = in.readString();
        remoteFileName = in.readString();
        bitmap = in.readParcelable(RoadCamera.class.getClassLoader());
    }

    public RoadCamera(Parcel in, ClassLoader classLoader){
        displayName = in.readString();
        remoteFileName = in.readString();
        bitmap = in.readParcelable(classLoader);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public void setRemoteFileName(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(remoteFileName);
        dest.writeParcelable(bitmap, flags);
    }

    public static final Parcelable.Creator<RoadCamera> CREATOR = new Parcelable.ClassLoaderCreator<RoadCamera>(){

        @Override
        public RoadCamera createFromParcel(Parcel source) {
            return new RoadCamera(source);
        }

        @Override
        public RoadCamera[] newArray(int size) {
            return new RoadCamera[size];
        }

        @Override
        public RoadCamera createFromParcel(Parcel source, ClassLoader loader) {
            return new RoadCamera(source, loader);
        }
    };
}
