package com.vejkamera;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ad on 27-05-2015.
 */
public class RoadCamera implements Parcelable{
    private String syncId = null;
    private String title = null;
    private String info = null;
    private String imageLink = null;
    private String thumbnailLink = null;
    private Double longitude = null;
    private Double latitude = null;
    private String state = null;
    private Long time = null;
    private Integer direction = null;
    private Bitmap bitmap = null;

    public RoadCamera() {
    }

    public RoadCamera(String displayName, String remoteFileName) {
        this.title = displayName;
        this.imageLink = remoteFileName;
    }


    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public RoadCamera(String displayName, String remoteFileName, Bitmap bitmap) {
        this.title = displayName;
        this.imageLink = remoteFileName;
        this.bitmap = bitmap;
    }

    public RoadCamera(Parcel in){
        title = in.readString();
        imageLink = in.readString();
        bitmap = in.readParcelable(RoadCamera.class.getClassLoader());
    }

    public RoadCamera(Parcel in, ClassLoader classLoader){
        title = in.readString();
        imageLink = in.readString();
        bitmap = in.readParcelable(classLoader);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
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
        dest.writeString(title);
        dest.writeString(imageLink);
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
