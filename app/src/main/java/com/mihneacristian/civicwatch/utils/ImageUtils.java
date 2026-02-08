package com.mihneacristian.civicwatch.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

    // Convert Bitmap to Base64 String (for saving to Firebase)
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Compress image to reduce size (80% quality, 800px max width)
        bitmap = resizeBitmap(bitmap, 800);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Convert Base64 String to Bitmap (for loading from Firebase)
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Load Base64 image into ImageView
    public static void loadBase64Image(ImageView imageView, String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return;
        }

        Bitmap bitmap = base64ToBitmap(base64String);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    // Resize bitmap to prevent oversized images
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth) {
        if (bitmap.getWidth() <= maxWidth) {
            return bitmap;
        }

        float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        int newHeight = (int) (maxWidth * aspectRatio);

        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
    }
}