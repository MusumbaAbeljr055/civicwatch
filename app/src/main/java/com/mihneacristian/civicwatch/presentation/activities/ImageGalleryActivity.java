package com.mihneacristian.civicwatch.presentation.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.ActivityImageGalleryBinding;
import com.mihneacristian.civicwatch.presentation.adapters.GalleryGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryActivity extends AppCompatActivity {

    private ActivityImageGalleryBinding binding;
    private GalleryGridAdapter adapter;
    private List<Bitmap> allImages = new ArrayList<>();
    private List<String> imageTitles = new ArrayList<>();
    private ArrayList<String> originalBase64Images;
    private static final String TAG = "ImageGalleryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure we're using the correct theme
        setTheme(R.style.Theme_CivicWatch);

        binding = ActivityImageGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Image Gallery");
        }

        // Get all images from intent
        originalBase64Images = getIntent().getStringArrayListExtra("all_images");
        ArrayList<String> titles = getIntent().getStringArrayListExtra("all_titles");

        // Debug logging
        Log.d(TAG, "Received images count: " + (originalBase64Images != null ? originalBase64Images.size() : 0));

        if (originalBase64Images != null && !originalBase64Images.isEmpty()) {
            for (int i = 0; i < originalBase64Images.size(); i++) {
                String base64 = originalBase64Images.get(i);
                Log.d(TAG, "Image " + i + " Base64 length: " + (base64 != null ? base64.length() : 0));

                Bitmap bitmap = decodeBase64ToBitmap(base64);
                if (bitmap != null) {
                    allImages.add(bitmap);
                    Log.d(TAG, "Successfully decoded image " + i);
                } else {
                    Log.e(TAG, "Failed to decode image " + i);
                }
            }
        } else {
            Log.e(TAG, "No images received in intent");
            Toast.makeText(this, "No images to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (titles != null) {
            imageTitles.addAll(titles);
        }

        // Show message if no images were successfully decoded
        if (allImages.isEmpty()) {
            Log.e(TAG, "No images could be decoded");
            Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        updateImageCount();

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new GalleryGridAdapter(allImages, imageTitles, new GalleryGridAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                // Open fullscreen view when image is clicked
                openFullscreenViewer(position);
            }
        });

        int spanCount = getResources().getInteger(R.integer.gallery_grid_span);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        binding.recyclerView.setAdapter(adapter);
    }

    private void openFullscreenViewer(int position) {
        Intent intent = new Intent(this, FullscreenImageActivity.class);

        // Pass the original Base64 strings to fullscreen activity
        intent.putStringArrayListExtra("image_urls", originalBase64Images);
        intent.putExtra("position", position);

        startActivity(intent);
    }

    private void updateImageCount() {
        binding.textImageCount.setText(allImages.size() + " images");
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            String cleanBase64 = base64String;
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            cleanBase64 = cleanBase64.replaceAll("\\s", "");
            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}