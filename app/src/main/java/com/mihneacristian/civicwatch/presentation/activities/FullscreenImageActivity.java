package com.mihneacristian.civicwatch.presentation.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.ActivityFullscreenImageBinding;
import com.mihneacristian.civicwatch.presentation.adapters.FullscreenImageAdapter;

import java.util.ArrayList;

public class FullscreenImageActivity extends AppCompatActivity {

    private ActivityFullscreenImageBinding binding;
    private FullscreenImageAdapter adapter;
    private ArrayList<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar
        setSupportActionBar(binding.topBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Get data from intent
        imageUrls = getIntent().getStringArrayListExtra("image_urls");
        int currentPosition = getIntent().getIntExtra("position", 0);

        if (imageUrls == null || imageUrls.isEmpty()) {
            finish();
            return;
        }

        setupViewPager(imageUrls, currentPosition);
        setupClickListeners();
    }

    private void setupViewPager(ArrayList<String> imageUrls, int currentPosition) {
        adapter = new FullscreenImageAdapter(imageUrls);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(currentPosition, false);

        // Setup dots indicator
        new TabLayoutMediator(binding.tabDots, binding.viewPager,
                (tab, position) -> {}
        ).attach();

        // Update counter
        updateCounter(currentPosition, imageUrls.size());

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateCounter(position, imageUrls.size());
            }
        });
    }

    private void updateCounter(int position, int total) {
        binding.textCounter.setText((position + 1) + " / " + total);
    }

    private void setupClickListeners() {
        // Handle toolbar navigation click (back button)
        binding.topBar.setNavigationOnClickListener(v -> finish());

        binding.btnShare.setOnClickListener(v -> shareImage());
        binding.btnDownload.setOnClickListener(v -> downloadImage());
    }

    private void shareImage() {
        int currentItem = binding.viewPager.getCurrentItem();
        if (currentItem >= 0 && currentItem < imageUrls.size()) {
            String imageData = imageUrls.get(currentItem);

            try {
                // Decode Base64 to bitmap
                String base64String = imageData;
                if (base64String.contains(",")) {
                    base64String = base64String.substring(base64String.indexOf(",") + 1);
                }
                base64String = base64String.replaceAll("\\s", "");
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                // Save to cache and share
                java.io.File cachePath = new java.io.File(getCacheDir(), "images");
                cachePath.mkdirs();
                java.io.File imageFile = new java.io.File(cachePath, "share_image_" + currentItem + ".png");
                java.io.FileOutputStream stream = new java.io.FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();

                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        imageFile
                );

                android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(android.content.Intent.createChooser(shareIntent, "Share Image"));

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadImage() {
        int currentItem = binding.viewPager.getCurrentItem();
        if (currentItem >= 0 && currentItem < imageUrls.size()) {
            String imageData = imageUrls.get(currentItem);

            try {
                // Decode Base64 to bitmap
                String base64String = imageData;
                if (base64String.contains(",")) {
                    base64String = base64String.substring(base64String.indexOf(",") + 1);
                }
                base64String = base64String.replaceAll("\\s", "");
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                // Save to gallery
                String savedImageURL = android.provider.MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        "CivicWatch_Image_" + System.currentTimeMillis(),
                        "Civic Watch Issue Image"
                );

                if (savedImageURL != null) {
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error downloading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}