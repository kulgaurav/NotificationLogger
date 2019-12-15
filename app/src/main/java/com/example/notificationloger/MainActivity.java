package com.example.notificationloger;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.notificationloger.Entity.Notification;
import com.example.notificationloger.Misc.UtilsAndConst;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ANDROID_ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE = 200;
    private TextView tv_current_data;
    private DataCollectBroadcastReceiver dataCollectBroadcastReceiver;

    public GoogleApiClient mApiClient;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;


    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_current_data =  this.findViewById(R.id.tv_show_data);

        if(!isNotificationServiceEnabled()){
            buildNotificationServiceAlertDialog().show();
        }

        dataCollectBroadcastReceiver = new DataCollectBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UtilsAndConst.INTENT_ACTION);
        registerReceiver(dataCollectBroadcastReceiver,intentFilter);

        startService(new Intent(this, NotificationCollectorMonitorService.class));

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();

        db = FirebaseFirestore.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dataCollectBroadcastReceiver);
    }

    private void postNotificationData(Notification notification){
        tv_current_data.setText(notification.toString());

        fetchLastlocation();
        // Create a new notificationObj
        Map<String, Object> notificationObj = new HashMap<>();
        notificationObj.put("timestamp", notification.getTimestamp());
        notificationObj.put("postOrRemoval", notification.getPostOrRemoval());
        notificationObj.put("notificationID", notification.getId());
        notificationObj.put("maxConfidence", notification.getMaxConfidence());
        notificationObj.put("detectedActivity", notification.getDetectedActivity());


// Add a new document with a generated ID
        db.collection("user_Me")
                .add(notificationObj)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }



    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ANDROID_ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(), "Reopen the app and give permissions please!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
        return(alertDialogBuilder.create());
    }



    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public class DataCollectBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Notification receivedNotification = intent.getParcelableExtra(UtilsAndConst.NOTIFICATION_BUNDLE);
            if(receivedNotification != null)
                postNotificationData(receivedNotification);

        }
    }

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

    public static class OwnReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent i = new Intent(context, OwnReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

                if (Build.VERSION.SDK_INT >= 23) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 ), pi);
                } else if (Build.VERSION.SDK_INT >= 19) {
                    am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 ), pi);
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 ), pi);
                }
            }
        }

    }


    private void fetchLastlocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Location location = (Location)o;
                if (location != null)
                    currentLocation = location;
                Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "," +
                        currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastlocation();
                }
                break;
        }
    }

}


