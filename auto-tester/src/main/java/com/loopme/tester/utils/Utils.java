package com.loopme.tester.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.loopme.tester.Constants;


public class Utils {
    private static final long TIME_DELAY = 200;
    private static final long TIME_DELAY_LG = 800;
    private static final String REGEX = "\\d{1,9}";

    public static boolean isLg() {
        return TextUtils.equals(Build.MANUFACTURER, Constants.MANUFACTURER_LGE)
                || TextUtils.equals(Build.MANUFACTURER, Constants.MANUFACTURER_LG);
    }

    public static boolean isInt(String number) {
        return number.matches(REGEX);
    }

    public static int parseToInt(String number) {
        try {
            return Integer.valueOf(number);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean hasPermission(Activity activity, String permissionType) {
        int permission = ContextCompat.checkSelfPermission(activity, permissionType);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity, String permissionType, int requestCode) {
        if (!Utils.hasPermission(activity, permissionType)) {
            ActivityCompat.requestPermissions(activity, new String[]{permissionType}, requestCode);
        }
    }

    public static long getTimeDelay() {
        return TIME_DELAY_LG;
    }
}
