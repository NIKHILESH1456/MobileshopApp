package com.mobileshop.app.utils;

public class Constants {

    // IMPORTANT: change this to the email you will use to log in as ADMIN.
    // Whoever logs in with this exact email is treated as the shop admin.
    public static final String ADMIN_EMAIL = "sriramsrm1993@gmail.com";

    // Fallback WhatsApp number (with country code, no + or spaces) used the very first time,
    // before the admin sets one from the Admin Dashboard. e.g. "919876543210"
    public static final String DEFAULT_WHATSAPP_NUMBER = "919014352394";

    // Firebase Realtime Database node names
    public static final String NODE_USERS = "users";
    public static final String NODE_PRODUCTS = "products";
    public static final String NODE_ORDERS = "orders";
    public static final String NODE_SETTINGS = "settings";

    public static final String SETTINGS_LOGO_URL = "logoUrl";
    public static final String SETTINGS_WHATSAPP_NUMBER = "whatsappNumber";

    // Firebase Storage folders
    public static final String STORAGE_PRODUCT_IMAGES = "product_images";
    public static final String STORAGE_LOGO = "logo";
}
