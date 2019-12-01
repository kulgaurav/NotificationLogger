package com.example.notificationloger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.notificationloger.Entity.Notification;
import com.example.notificationloger.Misc.UtilsAndConst;


public class NotificationListener extends NotificationListenerService {

    private static final String NOTIFICATION_POSTED = "POSTED";
    private static final String NOTIFICATION_REMOVED = "REMOVED";

    private NLSReceiver nlsReceiver;


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
        int id = sbn.getId();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(UtilsAndConst.SP_ACTIVITY_RECOG, 0); // 0 - for private mode
        String detectedActivity = pref.getString(UtilsAndConst.ACT_REG_DETECTED, null);
        int confidence = pref.getInt(UtilsAndConst.ACT_REG_CONFIDENCE, -1);
        return new Notification(arrivalT,pkgName,"", id, confidence,detectedActivity); // Let the third postOrRemoval handled at call

    }



}
