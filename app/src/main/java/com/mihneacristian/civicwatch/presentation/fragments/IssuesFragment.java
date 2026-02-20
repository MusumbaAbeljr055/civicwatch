package com.mihneacristian.civicwatch.presentation.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.databinding.FragmentIssuesBinding;
import com.mihneacristian.civicwatch.presentation.activities.LogInActivity;
import com.mihneacristian.civicwatch.presentation.activities.MainActivity;
import com.mihneacristian.civicwatch.presentation.adapters.IssuesAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssuesFragment extends Fragment {

    private FragmentIssuesBinding binding;
    private IssuesAdapter adapter;
    private DatabaseReference databaseReference;
    private ValueEventListener issuesListener;
    private ValueEventListener statsListener;
    private static final String TAG = "IssuesFragment";
    private SharedPreferences sharedPreferences;
    private boolean isGuest = false;
    private String userId = "";
    private boolean showOnlyMyIssues = false;
    private String correctFieldName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentIssuesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        isGuest = sharedPreferences.getBoolean("is_guest", false);
        userId = sharedPreferences.getString("user_id", "");

        // Get the correct field name from ProfileFragment
        correctFieldName = sharedPreferences.getString("correct_user_field", "reporterId");
        Log.d(TAG, "Using field name: " + correctFieldName + " for user issues");

        // Check if we should show only user's issues (from profile)
        showOnlyMyIssues = sharedPreferences.getBoolean("show_only_my_issues", false);

        // Clear the preference after reading
        if (showOnlyMyIssues) {
            sharedPreferences.edit().remove("show_only_my_issues").apply();
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Load issues from Firebase
        loadIssuesFromFirebase();

        // Load stats
        loadStats();
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
                if (isGuest) {
                    showGuestPrompt("Sign in to upvote issues");
                } else {
                    upvoteIssue(issue);
                }
            }
        });

        if (binding != null && binding.recipesRecyclerView != null) {
            binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recipesRecyclerView.setAdapter(adapter);
        }
    }

    private void loadIssuesFromFirebase() {
        if (!isAdded()) return;

        showLoading();

        // Remove any existing listener
        if (issuesListener != null && databaseReference != null) {
            databaseReference.child("issues").removeEventListener(issuesListener);
        }

        // Create the query
        Query issuesQuery;

        // If showing only user's issues and not guest, filter by the correct field
        if (showOnlyMyIssues && !isGuest && !userId.isEmpty()) {
            Log.d(TAG, "Filtering issues by " + correctFieldName + " = " + userId);
            issuesQuery = databaseReference.child("issues")
                    .orderByChild(correctFieldName)
                    .equalTo(userId);
        } else {
            issuesQuery = databaseReference.child("issues");
        }

        issuesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded() || binding == null) return;

                List<Issue> issues = new ArrayList<>();

                for (DataSnapshot issueSnapshot : dataSnapshot.getChildren()) {
                    Issue issue = issueSnapshot.getValue(Issue.class);
                    if (issue != null) {
                        issue.setIssueId(issueSnapshot.getKey());
                        issues.add(issue);
                    }
                }

                // Sort by date (newest first)
                Collections.sort(issues, (o1, o2) -> {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date1 = format.parse(o1.getCreatedAt());
                        Date date2 = format.parse(o2.getCreatedAt());
                        if (date1 != null && date2 != null) {
                            return date2.compareTo(date1);
                        }
                        return 0;
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                if (isAdded() && binding != null) {
                    if (issues.isEmpty()) {
                        showEmptyState();
                        if (showOnlyMyIssues && binding.emptyStateText != null) {
                            binding.emptyStateText.setText("You haven't reported any issues yet.\nBe the first to report!");
                        }
                    } else {
                        showIssuesList(issues);
                        if (showOnlyMyIssues) {
                            Toast.makeText(getContext(), "Showing your " + issues.size() + " reports", Toast.LENGTH_SHORT).show();
                        }
                    }
                    updateStatsCounts(issues);
                }
                Log.d(TAG, "Loaded " + issues.size() + " issues from Firebase");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded() || binding == null) return;

                Log.e(TAG, "Error loading issues: " + databaseError.getMessage());
                showEmptyState();
                Toast.makeText(getContext(), "Failed to load issues: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        issuesQuery.addValueEventListener(issuesListener);
    }

    private void loadStats() {
        if (!isAdded()) return;

        // Remove any existing listener
        if (statsListener != null && databaseReference != null) {
            databaseReference.child("stats").child("totalIssues").removeEventListener(statsListener);
        }

        statsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded() || binding == null) return;

                Integer totalCount = dataSnapshot.getValue(Integer.class);
                if (binding.totalIssuesCount != null) {
                    binding.totalIssuesCount.setText(totalCount != null ? String.valueOf(totalCount) : "0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading stats: " + databaseError.getMessage());
            }
        };

        databaseReference.child("stats").child("totalIssues").addValueEventListener(statsListener);
    }

    private void setupClickListeners() {
        if (binding == null) return;

        // Report Issue button in empty state
        try {
            if (binding.btnReportIssueEmptyState != null) {
                binding.btnReportIssueEmptyState.setOnClickListener(v -> {
                    if (isGuest) {
                        showGuestPrompt("Sign in to report issues");
                    } else {
                        navigateToMapTab();
                    }
                });
            }
        } catch (Exception e) {
            Log.w(TAG, "Report issue button not found: " + e.getMessage());
        }

        // Filter button
        if (binding.btnFilter != null) {
            binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        }
    }

    private void showGuestPrompt(String message) {
        if (!isAdded()) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Guest Mode")
                .setMessage(message + "\n\nCreate an account to unlock all features!")
                .setPositiveButton("Sign In", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Stay as Guest", null)
                .show();
    }

    private void navigateToMapTab() {
        if (getActivity() != null) {
            View rootView = getActivity().findViewById(R.id.bottomNavigation);
            if (rootView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) rootView)
                        .setSelectedItemId(R.id.map);
            } else {
                Toast.makeText(getContext(),
                        "Please navigate to Map tab to report issue",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateStatsCounts(List<Issue> issues) {
        if (!isAdded() || binding == null) return;

        int resolvedCount = 0;
        for (Issue issue : issues) {
            if (issue != null && "RESOLVED".equals(issue.getStatus())) {
                resolvedCount++;
            }
        }
        if (binding.resolvedIssuesCount != null) {
            binding.resolvedIssuesCount.setText(String.valueOf(resolvedCount));
        }

        // Update total count if showing filtered results
        if (showOnlyMyIssues && binding.totalIssuesCount != null) {
            binding.totalIssuesCount.setText(String.valueOf(issues.size()));
        }
    }

    private void showLoading() {
        if (!isAdded() || binding == null) return;

        if (binding.emptyStateLayout != null) {
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
        if (binding.recipesRecyclerView != null) {
            binding.recipesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        if (!isAdded() || binding == null) return;

        if (binding.emptyStateLayout != null) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            // Update empty state text based on context
            if (showOnlyMyIssues && binding.emptyStateText != null) {
                binding.emptyStateText.setText("You haven't reported any issues yet.\nBe the first to report!");
            } else if (isGuest && binding.emptyStateText != null) {
                binding.emptyStateText.setText("No issues reported yet.\nSign in to be the first to report!");
            }
        }
        if (binding.recipesRecyclerView != null) {
            binding.recipesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void showIssuesList(List<Issue> issues) {
        if (!isAdded() || binding == null) return;

        if (binding.emptyStateLayout != null) {
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
        if (binding.recipesRecyclerView != null) {
            binding.recipesRecyclerView.setVisibility(View.VISIBLE);
        }
        adapter.setIssues(issues);
    }

    private void showIssueDetails(Issue issue) {
        if (!isAdded()) return;

        Toast.makeText(requireContext(),
                "Issue: " + issue.getTitle(),
                Toast.LENGTH_SHORT).show();
        // TODO: Navigate to issue details fragment
    }

    private void upvoteIssue(Issue issue) {
        if (issue.getIssueId() == null) {
            Toast.makeText(getContext(), "Cannot upvote: Issue ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("issues").child(issue.getIssueId()).child("upvotes")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    Integer currentUpvotes = dataSnapshot.getValue(Integer.class);
                    if (currentUpvotes == null) {
                        currentUpvotes = 0;
                    }
                    databaseReference.child("issues").child(issue.getIssueId()).child("upvotes")
                            .setValue(currentUpvotes + 1)
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Upvoted!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Failed to upvote", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to upvote", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showFilterDialog() {
        if (!isAdded()) return;

        String[] filterOptions = {"All Issues", "Pending", "In Progress", "Resolved", "By Severity", "By Category"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Filter Issues")
                .setItems(filterOptions, (dialog, which) -> {
                    String filter = filterOptions[which];
                    Toast.makeText(getContext(), "Filter: " + filter, Toast.LENGTH_SHORT).show();
                    // TODO: Implement filtering logic
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listeners to prevent memory leaks
        if (issuesListener != null && databaseReference != null) {
            databaseReference.child("issues").removeEventListener(issuesListener);
        }
        if (statsListener != null && databaseReference != null) {
            databaseReference.child("stats").child("totalIssues").removeEventListener(statsListener);
        }
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Additional cleanup
        if (issuesListener != null && databaseReference != null) {
            databaseReference.child("issues").removeEventListener(issuesListener);
        }
        if (statsListener != null && databaseReference != null) {
            databaseReference.child("stats").child("totalIssues").removeEventListener(statsListener);
        }
    }
}