package com.mihneacristian.civicwatch.presentation.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.databinding.FragmentMapBinding;
import com.mihneacristian.civicwatch.databinding.LayoutBottomSheetBinding;
import com.mihneacristian.civicwatch.utils.ImageUtils;
import com.mihneacristian.civicwatch.utils.MapMarkerUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1003;
    private static final int CAMERA_REQUEST = 2001;
    private static final int GALLERY_REQUEST = 2002;
    private static final String TAG = "MapFragment";
    private LatLng selectedLocation;
    private BottomSheetDialog currentBottomSheetDialog;
    private Bitmap selectedPhotoBitmap;
    private String photoBase64 = "";
    private DatabaseReference databaseReference;
    private SimpleDateFormat dateFormat;
    private ValueEventListener issuesListener; // Store listener to remove it later

    // Zoom controls
    private MaterialCardView cardZoomIn, cardZoomOut;
    private ImageView imgZoomIn, imgZoomOut;

    // Uganda coordinates (center of Uganda)
    private static final LatLng UGANDA_CENTER = new LatLng(1.3733, 32.2903);
    private static final float UGANDA_ZOOM_LEVEL = 7.0f;

    // Kampala coordinates (capital city)
    private static final LatLng KAMPALA = new LatLng(0.3476, 32.5825);

    // Zoom constants
    private static final float ZOOM_IN_AMOUNT = 1.0f;
    private static final float ZOOM_OUT_AMOUNT = -1.0f;
    private static final float MIN_ZOOM_LEVEL = 3.0f;
    private static final float MAX_ZOOM_LEVEL = 21.0f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "MapFragment onViewCreated called");

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            Log.d(TAG, "SupportMapFragment found, calling getMapAsync");
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "SupportMapFragment not found! Check your fragment_map.xml");
            Toast.makeText(requireContext(), "Map fragment not found", Toast.LENGTH_SHORT).show();
        }

        setupFloatingActionButton();
        setupLocationButton();
        setupZoomControls();
    }

    private void setupFloatingActionButton() {
        binding.fab.setOnClickListener(v -> {
            showReportBottomSheet();
        });
    }

    private void setupLocationButton() {
        binding.fabLocation.setOnClickListener(v -> {
            getCurrentLocation();
        });
    }

    private void setupZoomControls() {
        // Find zoom controls
        cardZoomIn = binding.getRoot().findViewById(R.id.cardZoomIn);
        cardZoomOut = binding.getRoot().findViewById(R.id.cardZoomOut);

        if (cardZoomIn != null) {
            cardZoomIn.setOnClickListener(v -> zoomIn());
        }

        if (cardZoomOut != null) {
            cardZoomOut.setOnClickListener(v -> zoomOut());
        }
    }

    private void zoomIn() {
        if (googleMap != null) {
            float currentZoom = googleMap.getCameraPosition().zoom;
            if (currentZoom < MAX_ZOOM_LEVEL) {
                googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            } else {
                Toast.makeText(requireContext(), "Maximum zoom level reached", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void zoomOut() {
        if (googleMap != null) {
            float currentZoom = googleMap.getCameraPosition().zoom;
            if (currentZoom > MIN_ZOOM_LEVEL) {
                googleMap.animateCamera(CameraUpdateFactory.zoomOut());
            } else {
                Toast.makeText(requireContext(), "Minimum zoom level reached", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void zoomTo(float zoomLevel) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
        }
    }

    private void showReportBottomSheet() {
        // Check if fragment is still attached
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Cannot show bottom sheet - fragment not attached");
            return;
        }

        try {
            if (currentBottomSheetDialog != null && currentBottomSheetDialog.isShowing()) {
                currentBottomSheetDialog.dismiss();
            }

            // Reset photo when opening new dialog
            selectedPhotoBitmap = null;
            photoBase64 = "";

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
            currentBottomSheetDialog = bottomSheetDialog;

            LayoutBottomSheetBinding bottomSheetBinding = LayoutBottomSheetBinding.inflate(getLayoutInflater());
            bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

            // Setup Spinners with adapters
            ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.types,
                    android.R.layout.simple_spinner_item
            );
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            bottomSheetBinding.type.setAdapter(typeAdapter);

            ArrayAdapter<CharSequence> severityAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.severity,
                    android.R.layout.simple_spinner_item
            );
            severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            bottomSheetBinding.severity.setAdapter(severityAdapter);

            // Setup photo click listener
            bottomSheetBinding.issuePhoto.setOnClickListener(v -> {
                showPhotoSelectionDialog(bottomSheetBinding.issuePhoto);
            });

            // Setup close button (if you added one)
            try {
                View btnClose = bottomSheetBinding.getRoot().findViewById(R.id.btnClose);
                if (btnClose != null) {
                    btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
                }
            } catch (Exception e) {
                Log.w(TAG, "Close button not found in layout");
            }

            // Setup Add Location button
            bottomSheetBinding.addLocation.setOnClickListener(v -> {
                if (selectedLocation != null) {
                    // Get values from EditText fields
                    String title = getEditTextValue(bottomSheetBinding, R.id.editTextTitle);
                    String description = getEditTextValue(bottomSheetBinding, R.id.editTextDescription);

                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter issue title", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save the issue with the selected location
                    saveIssue(bottomSheetBinding, title, description);
                    bottomSheetDialog.dismiss();
                } else {
                    Toast.makeText(requireContext(),
                            "Please select a location on the map first",
                            Toast.LENGTH_SHORT).show();
                }
            });

            bottomSheetDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing bottom sheet: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error opening report dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private String getEditTextValue(LayoutBottomSheetBinding binding, int editTextId) {
        try {
            // Try to find the EditText by ID
            View view = binding.getRoot().findViewById(editTextId);
            if (view instanceof android.widget.EditText) {
                return ((android.widget.EditText) view).getText().toString().trim();
            }
        } catch (Exception e) {
            Log.w(TAG, "EditText not found with ID: " + editTextId);
        }
        return "";
    }

    private void showPhotoSelectionDialog(RoundedImageView imageView) {
        // Check if fragment is still attached
        if (!isAdded() || getContext() == null) {
            return;
        }

        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Take Photo
                if (checkCameraPermission()) {
                    openCamera();
                }
            } else if (which == 1) {
                // Choose from Gallery
                if (checkStoragePermission()) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private boolean checkCameraPermission() {
        if (!isAdded() || getContext() == null) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (!isAdded() || getContext() == null) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void openCamera() {
        if (!isAdded()) {
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void openGallery() {
        if (!isAdded()) {
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!isAdded()) {
            return;
        }

        if (resultCode == android.app.Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST && data != null) {
                // Handle camera photo
                selectedPhotoBitmap = (Bitmap) data.getExtras().get("data");
                if (selectedPhotoBitmap != null) {
                    photoBase64 = ImageUtils.bitmapToBase64(selectedPhotoBitmap);
                    updatePhotoInBottomSheet(selectedPhotoBitmap);
                }
            } else if (requestCode == GALLERY_REQUEST && data != null) {
                // Handle gallery photo
                Uri selectedImage = data.getData();
                try {
                    selectedPhotoBitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().getContentResolver(), selectedImage);
                    if (selectedPhotoBitmap != null) {
                        photoBase64 = ImageUtils.bitmapToBase64(selectedPhotoBitmap);
                        updatePhotoInBottomSheet(selectedPhotoBitmap);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error loading gallery image: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updatePhotoInBottomSheet(Bitmap bitmap) {
        if (currentBottomSheetDialog != null && currentBottomSheetDialog.isShowing()) {
            View dialogView = currentBottomSheetDialog.getWindow().getDecorView();
            RoundedImageView photoView = dialogView.findViewById(R.id.issuePhoto);
            if (photoView != null) {
                photoView.setImageBitmap(bitmap);
            }
        }
    }

    private void saveIssue(LayoutBottomSheetBinding binding, String title, String description) {
        // Check if fragment is attached
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Cannot save issue - fragment not attached");
            return;
        }

        try {
            // Get values from the form
            String issueType = binding.type.getSelectedItem().toString();
            String severity = binding.severity.getSelectedItem().toString();

            // Log for debugging
            Log.d(TAG, "Saving issue - Title: " + title +
                    ", Type: " + issueType +
                    ", Severity: " + severity +
                    ", Has Photo: " + (!photoBase64.isEmpty()));

            // Get custom marker icon based on issue type
            BitmapDescriptor markerIcon = MapMarkerUtils.getMarkerIcon(
                    requireContext(),
                    issueType
            );

            Log.d(TAG, "Marker icon obtained for type: " + issueType);

            // Add a marker on the map for the reported issue with custom icon
            if (googleMap != null && selectedLocation != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(selectedLocation)
                        .title(title)
                        .snippet("Type: " + issueType + "\nSeverity: " + severity);

                // Set the custom icon
                if (markerIcon != null) {
                    markerOptions.icon(markerIcon);
                    Log.d(TAG, "Custom icon set successfully");
                } else {
                    Log.w(TAG, "Marker icon is null, using default marker");
                }

                googleMap.addMarker(markerOptions);

                // Move camera to show the new marker
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));

                Log.d(TAG, "Marker added to map at: " + selectedLocation);
            }

            // Generate unique ID for the issue
            String issueId = databaseReference.child("issues").push().getKey();
            if (issueId == null) {
                Toast.makeText(requireContext(), "Error creating issue ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create issue object for Firebase
            Issue issue = new Issue(
                    title,
                    description,
                    issueType,
                    severity,
                    selectedLocation.latitude,
                    selectedLocation.longitude,
                    getAddressFromLocation(selectedLocation),
                    photoBase64,
                    getCurrentUserId(),
                    getCurrentUserName(),
                    getCurrentUserEmail()
            );

            // Set issue ID and timestamp
            issue.setIssueId(issueId);
            issue.setCreatedAt(dateFormat.format(new Date()));
            issue.setUpdatedAt(dateFormat.format(new Date()));

            // Save to Firebase
            databaseReference.child("issues").child(issueId).setValue(issue)
                    .addOnSuccessListener(aVoid -> {
                        String message = String.format("Issue reported successfully!\n%s\nType: %s\nSeverity: %s",
                                title, issueType, severity);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Issue saved to Firebase with ID: " + issueId);

                        // Update stats
                        updateStats();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving issue to Firebase: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Error saving issue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error saving issue: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error saving issue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadIssuesFromFirebase() {
        if (googleMap == null) {
            Log.w(TAG, "GoogleMap is null, cannot load issues");
            return;
        }

        Log.d(TAG, "Loading issues from Firebase...");

        // Store the listener so we can remove it later
        issuesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // CHECK IF FRAGMENT IS ATTACHED BEFORE UPDATING UI
                if (!isAdded() || getContext() == null || googleMap == null) {
                    Log.d(TAG, "Fragment not attached or map not ready, skipping update");
                    return;
                }

                Log.d(TAG, "Firebase data changed, loading issues...");

                try {
                    // Clear existing markers except Kampala
                    googleMap.clear();

                    // Re-add Kampala marker
                    googleMap.addMarker(new MarkerOptions()
                            .position(KAMPALA)
                            .title("Kampala")
                            .snippet("Capital of Uganda"));

                    int issueCount = 0;
                    for (DataSnapshot issueSnapshot : dataSnapshot.getChildren()) {
                        Issue issue = issueSnapshot.getValue(Issue.class);
                        if (issue != null) {
                            addIssueMarkerToMap(issue);
                            issueCount++;
                        }
                    }

                    Log.d(TAG, "Loaded " + issueCount + " issues from Firebase");

                    if (issueCount > 0 && isAdded()) {
                        // Use getContext() instead of requireContext() for safety
                        Context context = getContext();
                        if (context != null) {
                            Toast.makeText(context,
                                    "Loaded " + issueCount + " reported issues",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating map with issues: " + e.getMessage(), e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading issues from Firebase: " + databaseError.getMessage());
            }
        };

        // Attach the listener
        databaseReference.child("issues").addValueEventListener(issuesListener);
    }

    private void addIssueMarkerToMap(Issue issue) {
        // CHECK IF FRAGMENT IS ATTACHED AND MAP IS READY
        if (!isAdded() || getContext() == null || googleMap == null) return;

        try {
            LatLng issueLocation = new LatLng(issue.getLatitude(), issue.getLongitude());

            // Get custom marker icon based on issue type - use getContext() instead of requireContext()
            BitmapDescriptor markerIcon = MapMarkerUtils.getMarkerIcon(
                    getContext(),  // SAFE: getContext() returns null if not attached
                    issue.getCategory()
            );

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(issueLocation)
                    .title(issue.getTitle())
                    .snippet("Type: " + issue.getCategory() +
                            "\nSeverity: " + issue.getSeverity() +
                            "\nStatus: " + issue.getStatus());

            // Set the custom icon
            if (markerIcon != null) {
                markerOptions.icon(markerIcon);
            }

            googleMap.addMarker(markerOptions);

        } catch (Exception e) {
            Log.e(TAG, "Error adding issue marker to map: " + e.getMessage(), e);
        }
    }

    private String getAddressFromLocation(LatLng latLng) {
        return String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude);
    }

    private String getCurrentUserId() {
        return "user_" + System.currentTimeMillis();
    }

    private String getCurrentUserName() {
        return "Anonymous User";
    }

    private String getCurrentUserEmail() {
        return "";
    }

    private void updateStats() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        databaseReference.child("stats").child("totalIssues")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    Integer currentCount = dataSnapshot.getValue(Integer.class);
                    if (currentCount == null) {
                        currentCount = 0;
                    }
                    databaseReference.child("stats").child("totalIssues").setValue(currentCount + 1);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating stats: " + e.getMessage());
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady called - GoogleMap is ready");
        this.googleMap = googleMap;

        // Configure map settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        // Set Uganda as default view
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UGANDA_CENTER, UGANDA_ZOOM_LEVEL));

        // Add a marker for Kampala
        googleMap.addMarker(new MarkerOptions()
                .position(KAMPALA)
                .title("Kampala")
                .snippet("Capital of Uganda"));

        // Load existing issues from Firebase
        loadIssuesFromFirebase();

        // Enable UI controls
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission granted, enabling location features");
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Get current location
            getCurrentLocation();
        } else {
            Log.d(TAG, "Requesting location permission");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set map click listener - Save selected location when user taps map
        googleMap.setOnMapClickListener(latLng -> {
            // Check if fragment is attached
            if (!isAdded() || getContext() == null) {
                return;
            }

            // Save the selected location
            selectedLocation = latLng;

            // Don't clear all markers - just add a temporary marker
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Report Issue Here")
                    .snippet("Click + button to report issue at this location"));

            Toast.makeText(requireContext(),
                    "Location selected: " + latLng.latitude + ", " + latLng.longitude +
                            "\nNow click the + button to report an issue",
                    Toast.LENGTH_SHORT).show();
        });

        // Set map long click listener as alternative
        googleMap.setOnMapLongClickListener(latLng -> {
            if (isAdded() && getContext() != null) {
                selectedLocation = latLng;
                showReportBottomSheet();
            }
        });

        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(),
                    "Map loaded with existing issues!\nTap to select location, then click +",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Getting current location");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null && isAdded()) {
                            Log.d(TAG, "Location found: " + location.getLatitude() + ", " + location.getLongitude());
                            LatLng currentLatLng = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            // Move to user's current location with a zoom level
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                            // Don't clear markers - just add "Your Location" marker
                            googleMap.addMarker(new MarkerOptions()
                                    .position(currentLatLng)
                                    .title("Your Location"));

                            // Set as selected location
                            selectedLocation = currentLatLng;

                            Toast.makeText(requireContext(),
                                    "Found your location! Click + to report issue here",
                                    Toast.LENGTH_SHORT).show();
                        } else if (isAdded()) {
                            Log.w(TAG, "Location is null, staying on Uganda view");
                            Toast.makeText(requireContext(),
                                    "Could not get current location. Tap on map to select location.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Log.e(TAG, "Failed to get location: " + e.getMessage());
                            Toast.makeText(requireContext(),
                                    "Failed to get location. Tap on map to select location.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (isAdded()) {
            Toast.makeText(requireContext(),
                    "Location permission required. Please grant permission in settings.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (!isAdded()) {
            return;
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted");
                if (googleMap != null) {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    getCurrentLocation();
                }
            } else {
                Log.w(TAG, "Location permission denied");
                Toast.makeText(requireContext(),
                        "Location permission denied. Tap on map to select location.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove Firebase listener
        if (issuesListener != null) {
            databaseReference.child("issues").removeEventListener(issuesListener);
        }

        // Dismiss bottom sheet if showing
        if (currentBottomSheetDialog != null && currentBottomSheetDialog.isShowing()) {
            currentBottomSheetDialog.dismiss();
        }

        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Final cleanup
        if (issuesListener != null) {
            databaseReference.child("issues").removeEventListener(issuesListener);
        }
    }
}