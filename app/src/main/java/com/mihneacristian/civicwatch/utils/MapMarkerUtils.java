package com.mihneacristian.civicwatch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.mihneacristian.civicwatch.R;

public class MapMarkerUtils {

    private static final String TAG = "MapMarkerUtils";

    public static BitmapDescriptor getMarkerIcon(Context context, String issueType) {
        try {
            Log.d(TAG, "Getting marker icon for issue type: " + issueType);

            int drawableId = getDrawableForIssueType(issueType);
            Log.d(TAG, "Drawable ID: " + drawableId);

            Drawable drawable = context.getDrawable(drawableId);

            if (drawable == null) {
                Log.e(TAG, "Drawable is null for ID: " + drawableId + ", issue type: " + issueType);
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }

            Log.d(TAG, "Drawable found, creating bitmap descriptor");
            return getBitmapDescriptorFromDrawable(drawable);

        } catch (Exception e) {
            Log.e(TAG, "Error getting marker icon: " + e.getMessage(), e);
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        }
    }

    private static int getDrawableForIssueType(String issueType) {
        if (issueType == null) {
            return R.drawable.ic_map_marker;
        }

        switch (issueType) {
            case "Pothole":
                return R.drawable.ic_pothole;
            case "Graffiti":
                return R.drawable.ic_paint_spray;
            case "Litter":
                return R.drawable.ic_paper_bin;
            case "Illegal parking":
                return R.drawable.ic_no_parking;
            case "Roadworks":
                return R.drawable.ic_road_work;
            case "Street lighting":
                return R.drawable.ic_street_light;
            case "Illegal dumping":
                return R.drawable.ic_trash;
            case "Abandoned vehicle":
                return R.drawable.ic_crash;
            case "Damaged tree":
                return R.drawable.ic_dead_tree;
            case "Fallen tree":
                return R.drawable.ic_wind;
            case "Hanging branches":
                return R.drawable.ic_branch;
            case "Worn out street sign":
                return R.drawable.ic_traffic_signal;
            case "Other":
                return R.drawable.ic_map_marker;
            default:
                Log.w(TAG, "Unknown issue type: " + issueType);
                return R.drawable.ic_map_marker;
        }
    }

    private static BitmapDescriptor getBitmapDescriptorFromDrawable(Drawable drawable) {
        try {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();

            // Ensure minimum size
            if (width <= 0) width = 100;
            if (height <= 0) height = 100;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error creating bitmap from drawable: " + e.getMessage(), e);
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        }
    }
}