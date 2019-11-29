package com.example.notificationloger;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;


public class NotificationListener extends NotificationListenerService {



    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Intent intent = new  Intent("com.example.notificationloger");
        intent.putExtra("Notification", sbn);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Intent intent = new  Intent("com.example.notificationloger");
        intent.putExtra("Notification Removed", sbn);
        sendBroadcast(intent);
    }



}
