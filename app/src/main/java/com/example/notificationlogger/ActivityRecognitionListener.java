package com.example.notificationlogger;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.notificationlogger.Misc.UtilsAndConst;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognitionListener extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ActivityRecognitionListener(String name) {
        super(name);
    }

    public ActivityRecognitionListener() {
        super("IntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }

    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        int maxConfidence = 0;
        String detectedActivity = "";
        for (DetectedActivity activity : probableActivities) {
            if (activity.getConfidence() > maxConfidence) {
                maxConfidence = activity.getConfidence();
                switch (activity.getType()) {
                    case DetectedActivity.IN_VEHICLE: {
                        detectedActivity = "In_Vehicle";
                        break;
                    }
                    case DetectedActivity.ON_BICYCLE: {
                        detectedActivity = "On Bicycle" ;
                        break;
                    }
                    case DetectedActivity.ON_FOOT: {
                        detectedActivity = "On Foot" ;
                        break;
                    }
                    case DetectedActivity.RUNNING: {
                        detectedActivity = "Running" ;
                        break;
                    }
                    case DetectedActivity.STILL: {
                        detectedActivity = "Still";
                        break;
                    }
                    case DetectedActivity.TILTING: {
                        detectedActivity = "Tilting";
                        break;
                    }
                    case DetectedActivity.WALKING: {
                        detectedActivity =  "Walking";
                        break;
                    }
                    case DetectedActivity.UNKNOWN: {
                        detectedActivity =  "Unknown" ;
                        break;
                    }
                }
            }
        }
        SharedPreferences pref = getApplicationContext().getSharedPreferences(UtilsAndConst.SHARED_PREF_LOGGER, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(UtilsAndConst.ACT_REG_DETECTED, detectedActivity);
        editor.putInt(UtilsAndConst.ACT_REG_CONFIDENCE, maxConfidence);
        editor.commit();
    }
}
