package com.example.notificationloger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServiceBackground extends Service {
    public ServiceBackground() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
