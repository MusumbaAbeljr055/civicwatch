package com.mihneacristian.civicwatch.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.ActivityLogInBinding;
import com.mihneacristian.civicwatch.utils.LocaleHelper;
import com.mihneacristian.civicwatch.utils.ThemeManager;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SharedPreferences sharedPreferences;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme and language - moved to background
        super.onCreate(savedInstanceState);

        // Inflate view immediately
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize preferences
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        // Defer heavy initialization
        handler.post(this::initializeApp);
    }

    private void initializeApp() {
        // Apply theme and language in background
        ThemeManager.applyTheme(new ThemeManager(this).getThemeMode());
        LocaleHelper.applyLanguage(this);

        setupGoogleSignIn();
        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);

            // Check if user is already signed in - do this in background
            new Thread(() -> {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    // User is already signed in, go to MainActivity
                    saveUserSession(account.getId(), account.getDisplayName(), account.getEmail(), false);
                    handler.post(() -> {
                        startActivity(new Intent(LogInActivity.this, MainActivity.class));
                        finish();
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        // Use custom sign in button
        binding.signInButton.setOnClickListener(v -> signIn());

        // Continue as Guest
        binding.skipTextView.setOnClickListener(v -> {
            showGuestConfirmationDialog();
        });
    }

    private void showGuestConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Continue as Guest")
                .setMessage("As a guest, you can view issues but won't be able to report new issues, comment, or upvote. Sign up anytime to unlock all features!")
                .setPositiveButton("Continue as Guest", (dialog, which) -> {
                    createGuestSession();
                    startActivity(new Intent(LogInActivity.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("Sign In", (dialog, which) -> {
                    signIn();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void createGuestSession() {
        String guestId = "guest_" + System.currentTimeMillis();
        sharedPreferences.edit()
                .putString("user_id", guestId)
                .putString("user_name", "Guest User")
                .putString("user_email", "")
                .putBoolean("is_guest", true)
                .apply();

        Toast.makeText(this, "Continuing as Guest", Toast.LENGTH_SHORT).show();
    }

    private void saveUserSession(String userId, String name, String email, boolean isGuest) {
        sharedPreferences.edit()
                .putString("user_id", userId)
                .putString("user_name", name)
                .putString("user_email", email)
                .putBoolean("is_guest", isGuest)
                .apply();
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
            saveUserSession(account.getId(), account.getDisplayName(), account.getEmail(), false);
            Toast.makeText(this, "Welcome " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LogInActivity.this, MainActivity.class));
            finish();
        } catch (ApiException e) {
            // Sign in failed
            Toast.makeText(this, "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}