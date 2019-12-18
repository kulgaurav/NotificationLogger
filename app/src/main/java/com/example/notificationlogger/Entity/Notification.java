package com.example.notificationlogger.Entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    private String timestamp;
    private String pkgName;
    private String postOrRemoval;
    private String notificationID;
    private int maxConfidence;
    private String detectedActivity;
    private int ringerMode;
    private int batteryPercentage;
    private int isConnected = 0; // 0 : False | 1 : True
    private int isConnectedWifi = 0; // 0 : False | 1 : True
    private int isConnectedMobile = 0;  // 0 : False | 1 : True
    //private int isConnectionFast = 0; // 0 : False | 1 : True
    private int screenLocked; // 0 : False | 1 : True | -1 : Unavailable




    public Notification(String timestamp, String pkgName, String postOrRemoval, String id, int maxConfidence, String detectedActivity, int ringerMode,
    int batteryPercentage, boolean isConnected, boolean isConnectedWifi, boolean isConnectedMobile, int screenLocked) {
        this.timestamp = timestamp;
        this.pkgName = pkgName;
        this.postOrRemoval = postOrRemoval;
        this.notificationID = id;
        this.maxConfidence = maxConfidence;
        this.detectedActivity = detectedActivity;
        this.ringerMode = ringerMode;
        this.batteryPercentage = batteryPercentage;

        if(isConnected)
            this.isConnected = 1;
        if(isConnectedWifi)
            this.isConnectedWifi = 1;
        if(isConnectedMobile)
            this.isConnectedMobile = 1;

        this.screenLocked = screenLocked;
    }

    public Notification(Parcel in) {
        timestamp = in.readString();
        pkgName = in.readString();
        postOrRemoval = in.readString();
        notificationID = in.readString();
        maxConfidence = in.readInt();
        detectedActivity = in.readString();
        ringerMode = in.readInt();
        batteryPercentage = in.readInt();
        isConnected = in.readInt();
        isConnectedWifi = in.readInt();
        isConnectedMobile = in.readInt();
        screenLocked = in.readInt();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPostOrRemoval() {
        return postOrRemoval;
    }

    public String getId() {
        return notificationID;
    }

    public int getMaxConfidence() {
        return maxConfidence;
    }

    public String getDetectedActivity() {
        return detectedActivity;
    }

    public int getRingerMode() {
        return ringerMode;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }


    public int getIsConnected() {
        return isConnected;
    }

    public int getIsConnectedWifi() {
        return isConnectedWifi;
    }

    public int getIsConnectedMobile() {
        return isConnectedMobile;
    }


    public int getScreenLocked() {
        return screenLocked;
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
        dest.writeString(notificationID);
        dest.writeInt(maxConfidence);
        dest.writeString(detectedActivity);
        dest.writeInt(ringerMode);
        dest.writeInt(batteryPercentage);
        dest.writeInt(isConnected);
        dest.writeInt(isConnectedWifi);
        dest.writeInt(isConnectedMobile);
        dest.writeInt(screenLocked);
    }

}
