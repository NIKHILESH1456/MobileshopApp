package com.mobileshop.app.notifications;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Handles subscribing/unsubscribing devices to the "new_products" FCM topic,
 * and requesting the runtime POST_NOTIFICATIONS permission required on Android 13+.
 *
 * Call subscribeUserToProductAlerts() once a regular user is logged in (e.g. in MainActivity).
 * The admin device is never subscribed, since the admin is the one sending the alerts.
 */
public class NotificationHelper {

    public static final String TOPIC_NEW_PRODUCTS = "new_products";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 501;

    public static void subscribeUserToProductAlerts(Activity activity) {
        requestNotificationPermissionIfNeeded(activity);
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_NEW_PRODUCTS);
    }

    public static void unsubscribeFromProductAlerts() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_NEW_PRODUCTS);
    }

    private static void requestNotificationPermissionIfNeeded(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
}
