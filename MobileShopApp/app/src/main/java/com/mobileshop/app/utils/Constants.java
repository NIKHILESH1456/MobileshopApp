package com.mobileshop.app.utils;

public class Constants {

    // IMPORTANT: change this to the email you will use to log in as ADMIN.
    // Whoever logs in with this exact email is treated as the shop admin.
    public static final String ADMIN_EMAIL = "admin@mobileshop.com";

    // Fallback WhatsApp number (with country code, no + or spaces) used the very first time,
    // before the admin sets one from the Admin Dashboard. e.g. "919876543210"
    public static final String DEFAULT_WHATSAPP_NUMBER = "910000000000";

    // Firebase Realtime Database node names
    public static final String NODE_USERS = "users";
    public static final String NODE_PRODUCTS = "products";
    public static final String NODE_ORDERS = "orders";
    public static final String NODE_SETTINGS = "settings";

    public static final String SETTINGS_LOGO_URL = "logoUrl";
    public static final String SETTINGS_WHATSAPP_NUMBER = "whatsappNumber";

    // ImgBB API key — get a free one at https://api.imgbb.com/ (just sign in with Google/email,
    // no card required) and paste it here. Used to host product photos and the shop logo,
    // replacing Firebase Storage (which now requires the paid Blaze plan).
    public static final String IMGBB_API_KEY = "PASTE_YOUR_IMGBB_API_KEY_HERE";
}
