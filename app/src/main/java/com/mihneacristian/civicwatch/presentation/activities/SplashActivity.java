package com.mihneacristian.civicwatch.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import com.mihneacristian.civicwatch.databinding.ActivitySplashBinding;
import com.mihneacristian.civicwatch.utils.LocaleHelper;
import com.mihneacristian.civicwatch.utils.ThemeManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 800; // 800ms
    private ActivitySplashBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;
    private boolean isNavigationScheduled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme and language BEFORE setting content view - these are lightweight
        ThemeManager.applyTheme(new ThemeManager(this).getThemeMode());
        LocaleHelper.applyLanguage(this);

        super.onCreate(savedInstanceState);

        // Inflate view binding
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Make views visible immediately
        binding.logoImageView.setVisibility(View.VISIBLE);
        binding.appNameTextView.setVisibility(View.VISIBLE);

        // Simple fade-in animation (lightweight)
        setupSimpleAnimation();

        // Delay transition to LoginActivity
        navigateToLoginActivity();
    }

    private void setupSimpleAnimation() {
        // Simple fade in animation - lightweight compared to property animations
        AlphaAnimation fadeIn = new AlphaAnimation(0.3f, 1.0f); // Start from lower opacity
        fadeIn.setDuration(400); // Shorter animation
        fadeIn.setFillAfter(true);
        binding.logoImageView.startAnimation(fadeIn);

        // Also fade in app name
        AlphaAnimation textFadeIn = new AlphaAnimation(0.0f, 1.0f);
        textFadeIn.setDuration(600);
        textFadeIn.setStartOffset(200);
        binding.appNameTextView.startAnimation(textFadeIn);
    }

    private void navigateToLoginActivity() {
        if (isNavigationScheduled) return;

        isNavigationScheduled = true;

        navigateRunnable = () -> {
            if (!isFinishing() && !isDestroyed()) {
                try {
                    Intent intent = new Intent(SplashActivity.this, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    // Smooth transition
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (Exception e) {
                    // Fallback if navigation fails
                    finishAffinity();
                }
            }
        };

        handler.postDelayed(navigateRunnable, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent memory leaks by removing callbacks
        if (handler != null && navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
        binding = null;
    }

    @Override
    public void onBackPressed() {
        // Completely disable back button during splash to prevent ANR
        // Do nothing - but add super call for safety
        super.onBackPressed();
    }
}