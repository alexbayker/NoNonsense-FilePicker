package com.nononsenseapps.filepicker.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.core.content.ContextCompat;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class PermissionGranter {

    private static final int PERMISSIONS_DIALOG_DELAY = 3000;
    private static final int GRANT_BUTTON_INDEX = 1;

    public static void allowPermissionsIfNeeded(Activity activity) {
        allowPermissionsIfNeeded(activity, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE });
    }

    public static void allowPermissionsIfNeeded(Activity activity, String[] permissionsNeeded) {
        try {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(activity, permissionsNeeded))) {
                sleep(PERMISSIONS_DIALOG_DELAY);
                UiDevice device = UiDevice.getInstance(getInstrumentation());
                UiObject allowPermissions = device.findObject(new UiSelector().clickable(true).index(GRANT_BUTTON_INDEX));
                if (allowPermissions.exists()) {
                    allowPermissions.click();
                }
            }
        } catch (UiObjectNotFoundException e) {
            System.out.println("There is no permissions dialog to interact with");
        }
    }

    private static boolean hasNeededPermission(Activity activity, String[] permissionsNeeded) {
        for (String permission : permissionsNeeded) {
            int permissionStatus = ContextCompat.checkSelfPermission(activity, permission);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot execute Thread.sleep()");
        }
    }
}