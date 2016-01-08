package com.vejkamera;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ad on 27-05-2015.
 */
public class RoadCamera implements Parcelable, Comparable<RoadCamera> {
    private String syncId = "";
    private String title = "";
    private String info = "";
    private String imageLink = "";
    private String thumbnailLink = "";
    private Double longitude = new Double(0.0);
    private Double latitude = new Double(0.0);
    private String state = "";
    private Long time = new Long(0L);
    private Integer direction = new Integer(0);
    private Bitmap bitmap = null;
    private Bitmap thumbnail = null;

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

    public RoadCamera(String title, String imageLink, Bitmap bitmap) {
        this.title = title;
        this.imageLink = imageLink;
        this.bitmap = bitmap;
    }

    public RoadCamera(Parcel in) {
        syncId = in.readString();
        title = in.readString();
        info = in.readString();
        imageLink = in.readString();
        thumbnailLink = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        state = in.readString();
        time = in.readLong();
        direction = in.readInt();
        bitmap = in.readParcelable(RoadCamera.class.getClassLoader());
        thumbnail = in.readParcelable(RoadCamera.class.getClassLoader());
    }

    public RoadCamera(Parcel in, ClassLoader classLoader) {
        syncId = in.readString();
        title = in.readString();
        info = in.readString();
        imageLink = in.readString();
        thumbnailLink = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        state = in.readString();
        time = in.readLong();
        direction = in.readInt();
        bitmap = in.readParcelable(classLoader);
        thumbnail = in.readParcelable(classLoader);
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

    public Bitmap getThumbnail() { return thumbnail; }

    public void setThumbnail(Bitmap thumbnail) { this.thumbnail = thumbnail; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(syncId);
        dest.writeString(title);
        dest.writeString(info);
        dest.writeString(imageLink);
        dest.writeString(thumbnailLink);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(state);
        dest.writeLong(time);
        dest.writeInt(direction);
        dest.writeParcelable(bitmap, flags);
        dest.writeParcelable(thumbnail, flags);
    }

    public static final Parcelable.Creator<RoadCamera> CREATOR = new Parcelable.ClassLoaderCreator<RoadCamera>() {

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

    public int compareTo(RoadCamera compareRoadCamera){
        return this.syncId.compareTo(compareRoadCamera.getSyncId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoadCamera other = (RoadCamera) obj;

        if ((this.syncId == null) ? (other.syncId != null) : !this.syncId.equals(other.syncId)) {
            return false;
        }
        return true;
    }
}
