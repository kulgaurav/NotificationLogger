package com.example.notificationlogger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.notificationlogger.Entity.Notification;
import com.example.notificationlogger.Misc.UtilsAndConst;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ServiceBackground extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id_NL";
    private static final String TAG = "MainActivity";



    public GoogleApiClient mApiClient;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private FirebaseFirestore db;
    private String toShowOnTop = "Thanks for your contribution! :)";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        createNotificationChannel();

        startForeground();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate(){
        initServices();
    }


    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(toShowOnTop)
                .setContentIntent(pendingIntent)
                .build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIF_CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void initServices(){


        //Set up notification data collector
        DataCollectBroadcastReceiver dataCollectBroadcastReceiver = new DataCollectBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UtilsAndConst.INTENT_ACTION);
        try {
            registerReceiver(dataCollectBroadcastReceiver, intentFilter);
        }
        catch (IllegalArgumentException  e){
            System.out.println("Already registered!");
        }
        startService(new Intent(this, NotificationCollectorMonitorService.class));

        //Set up GoogleApiClient for Activity Recognition
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();

        //Set up FireBase Database
        db = FirebaseFirestore.getInstance();

        //Set GPS location service
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

    }


    /*
        For Sending to DB
     */

    private void postNotificationData(Notification notification){
        StringBuilder toConsole = new StringBuilder();


        fetchLastLocation();
        // Create a new notificationObj
        Map<String, Object> notificationObj = new HashMap<>();
        notificationObj.put("Timestamp", notification.getTimestamp());
        notificationObj.put("PostOrRemoval", notification.getPostOrRemoval());
        notificationObj.put("NotificationKey", notification.getKey());
        notificationObj.put("MaxConfidence", notification.getMaxConfidence());
        notificationObj.put("DetectedActivity", notification.getDetectedActivity());
        notificationObj.put("RingerMode", notification.getRingerMode());

        notificationObj.put("BatteryPercentage", notification.getBatteryPercentage());
        notificationObj.put("isConnected", notification.getIsConnected());
        notificationObj.put("isConnectedWifi", notification.getIsConnectedWifi());
        notificationObj.put("isConnectedMobile", notification.getIsConnectedMobile());
        notificationObj.put("ScreenLocked", notification.getScreenLocked());
        notificationObj.put("NotificationID", notification.getNotificationId());


        if(currentLocation!=null){
            notificationObj.put("Latitude", currentLocation.getLatitude());
            notificationObj.put("Longitude", currentLocation.getLongitude());
        }
        else{
            notificationObj.put("Latitude", null);
            notificationObj.put("Longitude", null);
        }

        for(Map.Entry<String, Object> entry : notificationObj.entrySet()){
            toConsole.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences(UtilsAndConst.SHARED_PREF_LOGGER, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(UtilsAndConst.CONSOLE_MSG, toConsole.toString());
        editor.apply();
        String userEmail = pref.getString(UtilsAndConst.USER_EMAIL, null);

        if(userEmail == null) toShowOnTop = "On no! something went wrong. Please login again!";

        else toShowOnTop = "Thanks for your contribution! :)";
        // Add a new document with a generated ID
        db.collection("user_" + userEmail)
                .add(notificationObj)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "NotificationLogger saving with ID: " + documentReference.getId());
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(UtilsAndConst.SHARED_PREF_LOGGER, 0); // 0 - for private mode

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }



    public class DataCollectBroadcastReceiver extends BroadcastReceiver {

        public DataCollectBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Notification receivedNotification = intent.getParcelableExtra(UtilsAndConst.NOTIFICATION_BUNDLE);
            if(receivedNotification != null)
                postNotificationData(receivedNotification);

        }
    }

      /*
        For Activity Recognition Service -----------------------------------------------------------
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognitionListener.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 30000, pendingIntent );
        //ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this.getApplicationContext());


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*
       For GPS Location ---------------------------------------------------------------------------
    */
    private void fetchLastLocation() {

        Task task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Location location = (Location)o;
                if (location != null)
                    currentLocation = location;
                //Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "," +
                // currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
            }

        });
    }



    /*
        --------------------------------------------------------------------------------------------
     */

}
