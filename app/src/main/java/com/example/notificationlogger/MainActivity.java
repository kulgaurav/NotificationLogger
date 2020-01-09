package com.example.notificationlogger;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.notificationlogger.Misc.UtilsAndConst;
import com.example.notificationlogger.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ANDROID_ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";


    private static final int REQUEST_CODE = 200;
    private TextView tv_current_data;
    private TextView tv_know_more;

    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(UtilsAndConst.SHARED_PREF_LOGGER, 0); // 0 - for private mode
        userEmail = pref.getString(UtilsAndConst.USER_EMAIL, null);
        if(userEmail == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
        initServices();

        tv_know_more = findViewById(R.id.txt_know_more);
        ToggleButton toggle =  findViewById(R.id.btn_know_more);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tv_know_more.setVisibility(View.VISIBLE);
                    tv_know_more.setMovementMethod(new ScrollingMovementMethod());
                } else {
                    tv_know_more.setVisibility(View.INVISIBLE);
                }
            }
        });


        final Button buttonFetchData = findViewById(R.id.btnFetchData);
        buttonFetchData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please mail to gauravk@tcd.ie for all your saved data.",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private void initServices(){
        //Setting up view to display one notification
        tv_current_data =  this.findViewById(R.id.tv_show_data);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(UtilsAndConst.SHARED_PREF_LOGGER,0); // 0 - for private mode
        tv_current_data.setText(pref.getString(UtilsAndConst.CONSOLE_MSG, "No data saved currently!"));

        //Get access to notification service
        if(!isNotificationServiceEnabled()){
            buildNotificationServiceAlertDialog().show();
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    /*
        For Notification service #------------------------------------------------------------------
     */
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


    /*
         #------------------------------------------------------------------------------------------
     */



    /*
        For AutoStart and Running in BG ----------------------------------------------------------------
     */

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


    /*
        For GPS Location ---------------------------------------------------------------------------
     */
    private void fetchLastLocation() {


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
                //Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "," +
                // currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                }
                break;
        }
    }


}


