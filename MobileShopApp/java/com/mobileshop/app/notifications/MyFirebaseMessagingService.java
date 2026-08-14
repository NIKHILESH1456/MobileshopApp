package com.mobileshop.app.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mobileshop.app.MainActivity;
import com.mobileshop.app.R;

/**
 * Receives push notifications sent by the "New Product Notifier" Cloud Function
 * (see /functions/index.js) whenever the admin adds a new product.
 *
 * Users are subscribed to the "new_products" topic automatically the first time
 * they open the app as a regular (non-admin) user - see NotificationHelper.subscribeUserToProductAlerts().
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "new_products_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title;
        String body;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        } else {
            // Fallback if the message was sent as a pure data payload
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

        if (title == null) title = "New phone in stock!";
        if (body == null) body = "Check out the latest mobile added to the store.";

        showNotification(title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // No per-user token storage is needed for this app because we broadcast to the
        // "new_products" topic rather than individual devices. Nothing to do here.
    }

    private void showNotification(String title, String body) {
        createChannelIfNeeded();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "New Products",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alerts you when the shop adds a new mobile phone");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
