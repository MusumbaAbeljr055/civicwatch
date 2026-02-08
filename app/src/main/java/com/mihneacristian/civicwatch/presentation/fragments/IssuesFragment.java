package com.mihneacristian.civicwatch.presentation.fragments;

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

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.databinding.FragmentIssuesBinding;
import com.mihneacristian.civicwatch.presentation.activities.MainActivity;
import com.mihneacristian.civicwatch.presentation.adapters.IssuesAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssuesFragment extends Fragment {

    private FragmentIssuesBinding binding;
    private IssuesAdapter adapter;
    private DatabaseReference databaseReference;
    private static final String TAG = "IssuesFragment";

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
            public void onIssueMenuClick(Issue issue, View view) {
                // Already handled in adapter
            }

            @Override
            public void onUpvoteClick(Issue issue) {
                upvoteIssue(issue);
            }
        });

        binding.recipesRecyclView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recipesRecyclView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Report Issue button in empty state
        try {
            // Access the button directly using its ID
            com.google.android.material.button.MaterialButton btnReportIssueEmptyState =
                    binding.emptyStateLayout.findViewById(R.id.btnReportIssueEmptyState);

            if (btnReportIssueEmptyState != null) {
                btnReportIssueEmptyState.setOnClickListener(v -> {
                    // Navigate to MapFragment to report issue
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
                });
            }
        } catch (Exception e) {
            Log.w(TAG, "Report issue button not found in layout: " + e.getMessage());
        }

        // Filter button - Access directly through binding
        if (binding.btnFilter != null) {
            binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        } else {
            Log.w(TAG, "btnFilter not found in binding");
        }


    }

    private void loadIssuesFromFirebase() {
        showLoading();

        databaseReference.child("issues").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Issue> issues = new ArrayList<>();

                for (DataSnapshot issueSnapshot : dataSnapshot.getChildren()) {
                    Issue issue = issueSnapshot.getValue(Issue.class);
                    if (issue != null) {
                        // Set the issue ID from Firebase key
                        issue.setIssueId(issueSnapshot.getKey());
                        issues.add(issue);
                    }
                }

                // Sort by date (newest first)
                Collections.sort(issues, new Comparator<Issue>() {
                    @Override
                    public int compare(Issue o1, Issue o2) {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Date date1 = format.parse(o1.getCreatedAt());
                            Date date2 = format.parse(o2.getCreatedAt());
                            return date2.compareTo(date1); // Descending order
                        } catch (ParseException e) {
                            return 0;
                        }
                    }
                });

                if (issues.isEmpty()) {
                    showEmptyState();
                } else {
                    showIssuesList(issues);
                }

                // Update stats
                updateStatsCounts(issues);

                Log.d(TAG, "Loaded " + issues.size() + " issues from Firebase");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading issues: " + databaseError.getMessage());
                showEmptyState();
            }
        });
    }

    private void loadStats() {
        // Load total issues count
        databaseReference.child("stats").child("totalIssues").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer totalCount = dataSnapshot.getValue(Integer.class);
                if (totalCount != null) {
                    binding.totalIssuesCount.setText(String.valueOf(totalCount));
                } else {
                    binding.totalIssuesCount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading stats: " + databaseError.getMessage());
            }
        });
    }

    private void updateStatsCounts(List<Issue> issues) {
        // Update resolved count
        int resolvedCount = 0;
        for (Issue issue : issues) {
            if ("RESOLVED".equals(issue.getStatus())) {
                resolvedCount++;
            }
        }
        binding.resolvedIssuesCount.setText(String.valueOf(resolvedCount));
    }

    private void showLoading() {
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.recipesRecyclView.setVisibility(View.GONE);
        // Show loading indicator if you add one
    }

    private void showEmptyState() {
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
        binding.recipesRecyclView.setVisibility(View.GONE);
    }

    private void showIssuesList(List<Issue> issues) {
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.recipesRecyclView.setVisibility(View.VISIBLE);
        adapter.setIssues(issues);
    }

    private void showIssueDetails(Issue issue) {
        // Show issue details dialog or navigate to details fragment
        Toast.makeText(requireContext(),
                "Issue Details: " + issue.getTitle(),
                Toast.LENGTH_SHORT).show();

        // TODO: Implement issue details dialog or fragment
        // You could create a dialog showing full issue details
    }

    private void upvoteIssue(Issue issue) {
        if (issue.getIssueId() == null) {
            Toast.makeText(requireContext(), "Cannot upvote: Issue ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Increment upvote count in Firebase
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
                                Toast.makeText(requireContext(), "Upvoted!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to upvote", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to upvote", Toast.LENGTH_SHORT).show();
                });
    }

    private void showFilterDialog() {
        // Show filter options dialog
        String[] filterOptions = {"All Issues", "Pending", "In Progress", "Resolved", "By Severity", "By Category"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Filter Issues");
        builder.setItems(filterOptions, (dialog, which) -> {
            String filter = filterOptions[which];
            Toast.makeText(requireContext(), "Filter: " + filter, Toast.LENGTH_SHORT).show();
            // TODO: Implement filtering logic
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}