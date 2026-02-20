package com.mihneacristian.civicwatch.presentation.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.databinding.FragmentProfileBinding;
import com.mihneacristian.civicwatch.presentation.activities.LogInActivity;
import com.mihneacristian.civicwatch.presentation.activities.MainActivity;
import com.mihneacristian.civicwatch.presentation.adapters.IssuesAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;
    private IssuesAdapter adapter;
    private boolean isGuest = false;
    private String userId = "";
    private String googleId = "";
    private String userName = "";
    private String userEmail = "";
    private Uri userPhotoUrl = null;
    private static final String TAG = "ProfileFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadUserSession();
        loadGoogleProfilePicture();
        setupRecyclerView();
        loadUserStats();

        // Load user issues with both ID formats
        loadUserIssues();

        setupClickListeners();
    }

    private void loadUserSession() {
        isGuest = sharedPreferences.getBoolean("is_guest", true);
        userId = sharedPreferences.getString("user_id", "");
        userName = sharedPreferences.getString("user_name", "User");
        userEmail = sharedPreferences.getString("user_email", "");

        // Get Google account directly
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            googleId = account.getId();
            Log.d(TAG, "Google Account ID: " + googleId);
            Log.d(TAG, "Google Account Email: " + account.getEmail());
            Log.d(TAG, "Google Account Name: " + account.getDisplayName());
        }

        Log.d(TAG, "=== USER SESSION ===");
        Log.d(TAG, "User ID from prefs: " + userId);
        Log.d(TAG, "Google ID from account: " + googleId);
        Log.d(TAG, "User Name: " + userName);
        Log.d(TAG, "User Email: " + userEmail);
        Log.d(TAG, "Is Guest: " + isGuest);
        Log.d(TAG, "===================");

        displayUserInfo();
    }

    private void loadGoogleProfilePicture() {
        if (!isGuest) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
            if (account != null && account.getPhotoUrl() != null) {
                userPhotoUrl = account.getPhotoUrl();
                Log.d(TAG, "Loading profile picture from: " + userPhotoUrl.toString());

                Glide.with(this)
                        .load(userPhotoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.profileImageView);
            } else {
                binding.profileImageView.setImageResource(R.drawable.ic_person);
            }
        } else {
            binding.profileImageView.setImageResource(R.drawable.ic_person);
        }
    }

    private void displayUserInfo() {
        if (binding == null) return;

        binding.userNameTextView.setText(userName);
        binding.userEmailTextView.setText(userEmail);

        if (isGuest) {
            binding.guestBadge.setVisibility(View.VISIBLE);
            binding.signOutButton.setVisibility(View.GONE);
            binding.editProfileButton.setVisibility(View.GONE);
            binding.myReportsSection.setVisibility(View.GONE);
        } else {
            binding.guestBadge.setVisibility(View.GONE);
            binding.signOutButton.setVisibility(View.VISIBLE);
            binding.editProfileButton.setVisibility(View.VISIBLE);
            binding.myReportsSection.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new IssuesAdapter();
        adapter.setOnIssueClickListener(new IssuesAdapter.OnIssueClickListener() {
            @Override
            public void onIssueClick(Issue issue) {
                showIssueDetails(issue);
            }

            @Override
            public void onUpvoteClick(Issue issue) {
                Toast.makeText(getContext(), "Cannot upvote your own issue", Toast.LENGTH_SHORT).show();
            }
        });

        if (binding != null && binding.recyclerView != null) {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerView.setAdapter(adapter);
        }
    }

    private void loadUserIssues() {
        if (isGuest || !isAdded() || binding == null) return;

        Log.d(TAG, "Loading issues for user...");

        // Clear existing issues
        adapter.setIssues(new ArrayList<>());

        // Try multiple possible ID formats
        List<String> possibleIds = new ArrayList<>();

        // Add the Google ID if available
        if (googleId != null && !googleId.isEmpty()) {
            possibleIds.add(googleId);
            Log.d(TAG, "Will try Google ID: " + googleId);
        }

        // Add the stored user ID
        if (userId != null && !userId.isEmpty() && !userId.equals(googleId)) {
            possibleIds.add(userId);
            Log.d(TAG, "Will try stored user ID: " + userId);
        }

        if (possibleIds.isEmpty()) {
            Log.e(TAG, "No user IDs available to query");
            return;
        }

        // Try each possible ID
        for (String id : possibleIds) {
            queryIssuesByReporterId(id);
        }
    }

    private void queryIssuesByReporterId(String reporterId) {
        Log.d(TAG, "Querying issues with reporterId: " + reporterId);

        databaseReference.child("issues")
                .orderByChild("reporterId")
                .equalTo(reporterId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!isAdded() || binding == null) return;

                        long count = dataSnapshot.getChildrenCount();
                        Log.d(TAG, "Found " + count + " issues with reporterId: " + reporterId);

                        if (count > 0) {
                            List<Issue> userIssues = new ArrayList<>();

                            for (DataSnapshot issueSnapshot : dataSnapshot.getChildren()) {
                                Issue issue = issueSnapshot.getValue(Issue.class);
                                if (issue != null) {
                                    issue.setIssueId(issueSnapshot.getKey());
                                    userIssues.add(issue);
                                    Log.d(TAG, "Found issue: " + issue.getTitle());
                                }
                            }

                            // Sort by date (newest first)
                            Collections.sort(userIssues, (o1, o2) -> {
                                try {
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    Date date1 = format.parse(o1.getCreatedAt());
                                    Date date2 = format.parse(o2.getCreatedAt());
                                    if (date1 != null && date2 != null) {
                                        return date2.compareTo(date1);
                                    }
                                    return 0;
                                } catch (Exception e) {
                                    return 0;
                                }
                            });

                            updateUIWithIssues(userIssues);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error querying issues: " + databaseError.getMessage());
                    }
                });
    }

    private void updateUIWithIssues(List<Issue> userIssues) {
        requireActivity().runOnUiThread(() -> {
            if (binding != null && isAdded()) {
                if (userIssues.isEmpty()) {
                    binding.noReportsText.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                    Log.d(TAG, "No issues to display");
                } else {
                    binding.noReportsText.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    // Show only the 3 most recent issues in profile
                    List<Issue> recentIssues = userIssues.size() > 3 ?
                            userIssues.subList(0, 3) : userIssues;
                    adapter.setIssues(recentIssues);

                    // Update stats
                    binding.totalReportsTextView.setText(String.valueOf(userIssues.size()));

                    int resolved = 0;
                    for (Issue issue : userIssues) {
                        if ("RESOLVED".equals(issue.getStatus())) {
                            resolved++;
                        }
                    }
                    binding.resolvedReportsTextView.setText(String.valueOf(resolved));

                    Log.d(TAG, "Displaying " + recentIssues.size() + " issues in UI");
                }
            }
        });
    }

    private void loadUserStats() {
        if (isGuest || !isAdded() || binding == null) return;

        databaseReference.child("upvotes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded() || binding == null) return;

                        int totalUpvotes = 0;
                        for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                            totalUpvotes += issueSnapshot.getChildrenCount();
                        }
                        binding.upvotesReceivedTextView.setText(String.valueOf(totalUpvotes));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (isAdded() && binding != null) {
                            binding.upvotesReceivedTextView.setText("0");
                        }
                    }
                });
    }

    private void simpleDatabaseCheck() {
        databaseReference.child("issues").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    Toast.makeText(getContext(), "No issues in database!", Toast.LENGTH_LONG).show();
                    return;
                }

                StringBuilder msg = new StringBuilder();
                msg.append("Total Issues: ").append(snapshot.getChildrenCount()).append("\n\n");
                msg.append("Google ID: ").append(googleId).append("\n");
                msg.append("Stored ID: ").append(userId).append("\n\n");

                for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                    String title = issueSnapshot.child("title").getValue(String.class);
                    String reporterId = issueSnapshot.child("reporterId").getValue(String.class);
                    String issueId = issueSnapshot.getKey();

                    msg.append("Issue: ").append(title).append("\n");
                    msg.append("  ID: ").append(issueId).append("\n");
                    msg.append("  reporterId: ").append(reporterId).append("\n");

                    boolean matchesGoogle = reporterId != null && reporterId.equals(googleId);
                    boolean matchesStored = reporterId != null && reporterId.equals(userId);

                    if (matchesGoogle) {
                        msg.append("  ✓ MATCHES GOOGLE ID!\n");
                    } else if (matchesStored) {
                        msg.append("  ✓ MATCHES STORED ID!\n");
                    } else {
                        msg.append("  ✗ NO MATCH\n");
                    }
                    msg.append("\n");

                    Log.d(TAG, "Issue: " + title + ", reporterId: " + reporterId);
                }

                new AlertDialog.Builder(requireContext())
                        .setTitle("Database Check")
                        .setMessage(msg.toString())
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Copy", (dialog, which) -> {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("debug", msg.toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        if (binding == null) return;

        binding.signOutButton.setOnClickListener(v -> showSignOutDialog());

        binding.editProfileButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit profile coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.viewAllReportsButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                sharedPreferences.edit().putBoolean("show_only_my_issues", true).apply();
                ((MainActivity) getActivity()).getBottomNavigation().setSelectedItemId(R.id.issues);
            }
        });

        // Simple database check button
        Button simpleBtn = new Button(requireContext());
        simpleBtn.setText("📋 Check Database");
        simpleBtn.setTextSize(14);
        simpleBtn.setAllCaps(false);
        simpleBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        simpleBtn.setTextColor(getResources().getColor(android.R.color.white));

        ViewGroup.LayoutParams simpleParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        simpleBtn.setLayoutParams(simpleParams);
        simpleBtn.setOnClickListener(v -> simpleDatabaseCheck());

        ViewGroup parent = (ViewGroup) binding.editProfileButton.getParent();
        parent.addView(simpleBtn);
    }

    private void showSignOutDialog() {
        if (!isAdded()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> signOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signOut() {
        if (!isAdded()) return;

        sharedPreferences.edit().clear().apply();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }

    private void showIssueDetails(Issue issue) {
        if (isAdded()) {
            Toast.makeText(getContext(), "Issue: " + issue.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferences != null) {
            loadUserSession();
            loadGoogleProfilePicture();
            loadUserIssues();
        }
    }
}