package com.mihneacristian.civicwatch.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.ActivityMainBinding;
import com.mihneacristian.civicwatch.presentation.fragments.AboutUsFragment;
import com.mihneacristian.civicwatch.presentation.fragments.IssuesFragment;
import com.mihneacristian.civicwatch.presentation.fragments.MapFragment;
import com.mihneacristian.civicwatch.presentation.fragments.ProfileFragment;
import com.mihneacristian.civicwatch.presentation.fragments.SettingsFragment;
import com.mihneacristian.civicwatch.utils.LocaleHelper;
import com.mihneacristian.civicwatch.utils.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MapFragment mapFragment;
    private IssuesFragment issuesFragment;
    private AboutUsFragment aboutUsFragment;
    private SettingsFragment settingsFragment;
    private ProfileFragment profileFragment;
    private SharedPreferences sharedPreferences;
    private boolean isGuest = false;
    private String userName = "";
    private String userEmail = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply language to context before any setup
        super.attachBaseContext(LocaleHelper.updateResources(newBase,
                new LocaleHelper(newBase).getLanguage()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply language before theme
        LocaleHelper.applyLanguage(this);

        // Apply theme before setting content view
        ThemeManager.applyTheme(new ThemeManager(this).getThemeMode());

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        loadUserSession();

        setupToolbar();
        setupFragments();
        setupBottomNavigation();
        checkUserPermissions();

        // Load MapFragment by default
        if (savedInstanceState == null) {
            loadFragment(mapFragment);
            binding.bottomNavigation.setSelectedItemId(R.id.map);
        }
    }

    private void loadUserSession() {
        isGuest = sharedPreferences.getBoolean("is_guest", true);
        userName = sharedPreferences.getString("user_name", "Guest User");
        userEmail = sharedPreferences.getString("user_email", "");
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        updateToolbarTitle();
    }

    private void updateToolbarTitle() {
        if (getSupportActionBar() != null) {
            if (!isGuest) {
                getSupportActionBar().setTitle(userName);
                getSupportActionBar().setSubtitle("Signed in with Google");
            } else {
                getSupportActionBar().setTitle(getString(R.string.app_name));
                getSupportActionBar().setSubtitle("Guest Mode");
            }
        }
    }

    private void setupFragments() {
        // Initialize fragments
        mapFragment = new MapFragment();
        issuesFragment = new IssuesFragment();
        aboutUsFragment = new AboutUsFragment();
        settingsFragment = new SettingsFragment();
        profileFragment = new ProfileFragment();
    }

    private void setupBottomNavigation() {
        // Set up the bottom navigation with proper tinting for dark mode
        setupBottomNavigationTinting();

        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            String title = getString(R.string.app_name);

            int id = item.getItemId();
            if (id == R.id.map) {
                fragment = mapFragment;
                title = getString(R.string.map);
            } else if (id == R.id.issues) {
                fragment = issuesFragment;
                title = getString(R.string.reported_issues);
            } else if (id == R.id.profile) {
                fragment = profileFragment;
                title = "Profile";
            } else if (id == R.id.settings) {
                fragment = settingsFragment;
                title = getString(R.string.settings);
            } else if (id == R.id.aboutUs) {
                fragment = aboutUsFragment;
                title = getString(R.string.about_us);
            }

            if (fragment != null) {
                loadFragment(fragment);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                    if (id == R.id.profile) {
                        getSupportActionBar().setSubtitle(null);
                    } else {
                        updateToolbarSubtitle();
                    }
                }
                return true;
            }
            return false;
        });
    }

    private void updateToolbarSubtitle() {
        if (getSupportActionBar() != null) {
            if (!isGuest) {
                getSupportActionBar().setSubtitle("Signed in as " + userName);
            } else {
                getSupportActionBar().setSubtitle("Guest Mode");
            }
        }
    }

    private void setupBottomNavigationTinting() {
        // Create color state list for bottom navigation icons and text
        android.content.res.ColorStateList iconTint = createIconColorStateList();

        // Apply the tint
        binding.bottomNavigation.setItemIconTintList(iconTint);
        binding.bottomNavigation.setItemTextColor(iconTint);

        // Set background color based on theme
        if (ThemeManager.isDarkMode(this)) {
            binding.bottomNavigation.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.card_background)
            );
        } else {
            binding.bottomNavigation.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.background_white)
            );
        }
    }

    private android.content.res.ColorStateList createIconColorStateList() {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },  // checked
                new int[] { -android.R.attr.state_checked }  // unchecked
        };

        int[] colors = new int[] {
                ContextCompat.getColor(this, R.color.colorPrimary),  // checked color
                ContextCompat.getColor(this, R.color.icon_secondary) // unchecked color
        };

        return new android.content.res.ColorStateList(states, colors);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    private void checkUserPermissions() {
        if (isGuest) {
            // Show guest badge in toolbar
            updateToolbarSubtitle();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isGuest) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sign_out) {
            showSignOutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSignOutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> signOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signOut() {
        // Clear local session
        sharedPreferences.edit().clear().apply();

        // Sign out from Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to Login
            Intent intent = new Intent(this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    public boolean isGuest() {
        return isGuest;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void showSignInPrompt(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign In Required")
                .setMessage(message)
                .setPositiveButton("Sign In", (dialog, which) -> {
                    // Navigate to login
                    Intent intent = new Intent(this, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        // If we're not on the map, go back to map
        if (binding.bottomNavigation.getSelectedItemId() != R.id.map) {
            binding.bottomNavigation.setSelectedItemId(R.id.map);
        } else {
            super.onBackPressed();
        }
    }

    public BottomNavigationView getBottomNavigation() {
        return binding.bottomNavigation;
    }
}