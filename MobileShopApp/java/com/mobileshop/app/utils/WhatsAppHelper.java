package com.mobileshop.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class WhatsAppHelper {

    /**
     * Opens a WhatsApp chat with the admin's number, prefilled with the given message.
     * Works even if the exact contact isn't saved, using the wa.me deep link.
     *
     * @param context      calling context
     * @param phoneNumber  number with country code, digits only (e.g. "919876543210")
     * @param message      prefilled message text
     */
    public static void openChat(Context context, String phoneNumber, String message) {
        try {
            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show();
        }
    }
}
