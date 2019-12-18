package com.example.notificationlogger;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.notificationlogger.Entity.Notification;
import com.example.notificationlogger.Misc.UtilsAndConst;


public class NotificationListener extends NotificationListenerService {

    private static final String NOTIFICATION_POSTED = "Posted";
    private static final String NOTIFICATION_REMOVED = "Removed";

    private NLSReceiver nlsReceiver;
    private AudioManager mAudioManager;


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nlsReceiver = new NLSReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UtilsAndConst.INTENT_ACTION);
        registerReceiver(nlsReceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlsReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Notification notification = getCleanNotification(sbn);
        notification.setPostOrRemoval(NOTIFICATION_POSTED);
        Intent intent = new  Intent(UtilsAndConst.INTENT_ACTION);
        intent.putExtra(UtilsAndConst.NOTIFICATION_BUNDLE, notification);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Notification notification = getCleanNotification(sbn);
        notification.setPostOrRemoval(NOTIFICATION_REMOVED);
        Intent intent = new  Intent(UtilsAndConst.INTENT_ACTION);
        intent.putExtra(UtilsAndConst.NOTIFICATION_BUNDLE, notification);
        sendBroadcast(intent);
    }

    class NLSReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


        }
    }

    private Notification getCleanNotification(StatusBarNotification sbn){
        if(sbn == null)
            return null; // Send message with issue rather
        String pkgName = sbn.getPackageName();
        String arrivalT = UtilsAndConst.getTimeStampString(sbn.getPostTime());
        String id = sbn.getKey();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(UtilsAndConst.SHARED_PREF_LOGGER, 0); // 0 - for private mode
        String detectedActivity = pref.getString(UtilsAndConst.ACT_REG_DETECTED, null);
        int confidence = pref.getInt(UtilsAndConst.ACT_REG_CONFIDENCE, -1);

        // Get an instance of AudioManager system service
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = mAudioManager != null ? mAudioManager.getRingerMode() : -1;

        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int batteryPercentage = -1;
        if (android.os.Build.VERSION.SDK_INT >=  android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (bm != null) {
                batteryPercentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
        }

        KeyguardManager kgMgr = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        int screenLock = -1;
        if(kgMgr != null){
            if(kgMgr.isKeyguardLocked())
                screenLock = 1;
            else
                screenLock = 0;
        }

        return new Notification(arrivalT,pkgName,"", id, confidence,detectedActivity,ringerMode,batteryPercentage,
                Connectivity.isConnected(getApplicationContext()),
                Connectivity.isConnectedWifi(getApplicationContext()),
                Connectivity.isConnectedMobile(getApplicationContext()),
                screenLock); // Let the third param postOrRemoval handled at call

    }

}
