package com.example.notificationloger.Entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    private String timestamp;
    private String pkgName;
    private String postOrRemoval;
    private int id;

    public Notification(String timestamp, String pkgName, String postOrRemoval) {
        this.timestamp = timestamp;
        this.pkgName = pkgName;
        this.postOrRemoval = postOrRemoval;
    }

    public Notification(Parcel in) {
        timestamp = in.readString();
        pkgName = in.readString();
        postOrRemoval = in.readString();
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getPostOrRemoval() {
        return postOrRemoval;
    }

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

    }

    @Override
    public String toString() {
        return "Notification{" +
                "timestamp='" + timestamp + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", postOrRemoval='" + postOrRemoval + '\'' +
                '}';
    }
}
