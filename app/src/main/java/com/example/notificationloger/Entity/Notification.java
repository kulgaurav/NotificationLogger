package com.example.notificationloger.Entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    private String timestamp;
    private String pkgName;
    private String postOrRemoval;
    private int notificationID;
    private int maxConfidence;
    private String detectedActivity;
    private String latitude;
    private String longitude;


    public Notification(String timestamp, String pkgName, String postOrRemoval, int id, int maxConfidence, String detectedActivity) {
        this.timestamp = timestamp;
        this.pkgName = pkgName;
        this.postOrRemoval = postOrRemoval;
        this.notificationID = id;
        this.maxConfidence = maxConfidence;
        this.detectedActivity = detectedActivity;

    }

    public Notification(Parcel in) {
        timestamp = in.readString();
        pkgName = in.readString();
        postOrRemoval = in.readString();
        notificationID = in.readInt();
        maxConfidence = in.readInt();
        detectedActivity = in.readString();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPostOrRemoval() {
        return postOrRemoval;
    }

    public int getId() {
        return notificationID;
    }

    public int getMaxConfidence() {
        return maxConfidence;
    }

    public String getDetectedActivity() {
        return detectedActivity;
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };


    public void setPostOrRemoval(String postOrRemoval) {
        this.postOrRemoval = postOrRemoval;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeString(pkgName);
        dest.writeString(postOrRemoval);
        dest.writeInt(notificationID);
        dest.writeInt(maxConfidence);
        dest.writeString(detectedActivity);

    }

    @Override
    public String toString() {
        return "Notification{" +
                "timestamp='" + timestamp + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", postOrRemoval='" + postOrRemoval + '\'' +
                ", id=" + notificationID +
                ", maxConfidence=" + maxConfidence +
                ", detectedActivity='" + detectedActivity + '\'' +
                '}';
    }
}
