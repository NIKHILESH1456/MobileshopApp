package com.mobileshop.app.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Uploads an image to ImgBB (https://imgbb.com), a free image host, and returns the
 * public URL. Used in place of Firebase Storage, which now requires the paid Blaze plan
 * to create a bucket at all — ImgBB stays free with generous limits and no card needed.
 *
 * Get a free API key at https://api.imgbb.com/ and set it in Constants.IMGBB_API_KEY.
 */
public class ImgBBUploader {

    private static final String ENDPOINT = "https://api.imgbb.com/1/upload";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    /**
     * @param resolver ContentResolver (e.g. from an Activity) used to read the picked image
     * @param imageUri local content:// Uri picked from the gallery
     * @param callback fired on the main thread with the result
     */
    public static void upload(ContentResolver resolver, Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                String base64Image = readUriAsBase64(resolver, imageUri);

                RequestBody formBody = new FormBody.Builder()
                        .add("key", Constants.IMGBB_API_KEY)
                        .add("image", base64Image)
                        .build();

                Request request = new Request.Builder()
                        .url(ENDPOINT)
                        .post(formBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        postFailure(callback, "Network error: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try (Response resp = response) {
                            String bodyStr = resp.body() != null ? resp.body().string() : "";
                            if (!resp.isSuccessful()) {
                                postFailure(callback, "Upload failed (" + resp.code() + "): " + bodyStr);
                                return;
                            }
                            JSONObject json = new JSONObject(bodyStr);
                            if (!json.optBoolean("success", false)) {
                                postFailure(callback, "ImgBB rejected the upload: " + bodyStr);
                                return;
                            }
                            String url = json.getJSONObject("data").getString("url");
                            postSuccess(callback, url);
                        } catch (Exception e) {
                            postFailure(callback, "Could not parse response: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                postFailure(callback, "Could not read image: " + e.getMessage());
            }
        }).start();
    }

    private static String readUriAsBase64(ContentResolver resolver, Uri uri) throws IOException {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) throw new IOException("Unable to open image");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            return Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);
        }
    }

    private static void postSuccess(UploadCallback callback, String url) {
        mainHandler.post(() -> callback.onSuccess(url));
    }

    private static void postFailure(UploadCallback callback, String message) {
        mainHandler.post(() -> callback.onFailure(message));
    }
}
