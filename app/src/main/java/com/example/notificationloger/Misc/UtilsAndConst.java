package com.example.notificationloger.Misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UtilsAndConst {

    public static final String INTENT_ACTION = "com.example.notificationloger";
    public static final String NOTIFICATION_BUNDLE = "Notification";

    public static final String SHARED_PREF_LOGGER = "notification_logger_sharedPref";
    public static final String ACT_REG_CONFIDENCE = "sp_act_reg_confidence";
    public static final String ACT_REG_DETECTED = "sp_act_reg_detected";
    public static final String CONSOLE_MSG = "console_msg";




    public static String getTimeStampString(long currentTimeMillis){
        String toReturn = "";
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        toReturn = simpleDateFormat.format(date);
        return toReturn;
    }
}
