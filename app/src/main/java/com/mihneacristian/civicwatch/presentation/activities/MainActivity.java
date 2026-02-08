
package com.mihneacristian.civicwatch.presentation.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.ActivityMainBinding;
import com.mihneacristian.civicwatch.presentation.fragments.AboutUsFragment;
import com.mihneacristian.civicwatch.presentation.fragments.IssuesFragment;
import com.mihneacristian.civicwatch.presentation.fragments.MapFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MapFragment mapFragment;
    private IssuesFragment issuesFragment;
    private AboutUsFragment aboutUsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFragments();
        setupBottomNavigation();

        // Load MapFragment by default
        if (savedInstanceState == null) {
            loadFragment(mapFragment);
            binding.bottomNavigation.setSelectedItemId(R.id.map);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("CivicWatch");
        }
    }

    private void setupFragments() {
        // Initialize fragments
        mapFragment = new MapFragment();
        issuesFragment = new IssuesFragment();
        aboutUsFragment = new AboutUsFragment();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            String title = "CivicWatch";

            int id = item.getItemId();
            if (id == R.id.map) {
                fragment = mapFragment;
                title = "Issues Map";
            } else if (id == R.id.issues) {
                fragment = issuesFragment;
                title = "Reported Issues"; // Changed from "My Issues" to match your layout
            } else if (id == R.id.aboutUs) {
                fragment = aboutUsFragment;
                title = "About Us";
            } else if (id == R.id.contactUs) {
                openContactEmail();
                return true;
            }

            if (fragment != null) {
                loadFragment(fragment);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    private void openContactEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.author_email)});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.author_email));
            intent.putExtra(Intent.EXTRA_TEXT, "\n" +
                    "Manufacturer: " + Build.MANUFACTURER.toUpperCase() +
                    " \nModel: " + Build.MODEL.toUpperCase());
            startActivity(Intent.createChooser(intent, ""));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
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

    // ADD THIS METHOD - It's called by IssuesFragment
    public BottomNavigationView getBottomNavigation() {
        return binding.bottomNavigation;
    }
}