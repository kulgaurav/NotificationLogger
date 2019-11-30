package com.example.notificationloger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.notificationloger.Entity.Notification;
import com.example.notificationloger.Misc.Utils;


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
        filter.addAction(Utils.INTENT_ACTION);
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
        Intent intent = new  Intent(Utils.INTENT_ACTION);
        intent.putExtra(Utils.NOTIFICATION_BUNDLE, notification);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Notification notification = getCleanNotification(sbn);
        notification.setPostOrRemoval(NOTIFICATION_REMOVED);
        Intent intent = new  Intent(Utils.INTENT_ACTION);
        intent.putExtra(Utils.NOTIFICATION_BUNDLE, notification);
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
        String arrivalT = Utils.getTimeStampString(sbn.getPostTime());

        String key = sbn.getKey();
        int id = sbn.getId();
        System.out.println("Key:" + key + "  id:" + id);
        return new Notification(arrivalT,pkgName,""); // Let the third param handled at call

    }



}
