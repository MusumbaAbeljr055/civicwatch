package com.mihneacristian.civicwatch.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.data.model.User;

public class FirebaseDatabaseService {

    private static final String TAG = "FirebaseDatabaseService";
    private final DatabaseReference databaseReference;

    public FirebaseDatabaseService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
    }

    // Save a new issue
    public void saveIssue(Issue issue, DatabaseCallback callback) {
        String issueId = databaseReference.child("issues").push().getKey();
        if (issueId != null) {
            issue.setIssueId(issueId);

            // Log the data being saved
            Log.d(TAG, "=== SAVING ISSUE ===");
            Log.d(TAG, "Issue ID: " + issueId);
            Log.d(TAG, "Title: " + issue.getTitle());
            Log.d(TAG, "Reporter ID: " + issue.getReporterId());
            Log.d(TAG, "Reporter Name: " + issue.getReporterName());
            Log.d(TAG, "===================");

            databaseReference.child("issues").child(issueId).setValue(issue)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Issue saved successfully with ID: " + issueId);
                        updateStats();
                        callback.onSuccess(issueId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save issue: " + e.getMessage());
                        callback.onFailure(e);
                    });
        } else {
            callback.onFailure(new Exception("Failed to generate issue ID"));
        }
    }

    // Get all issues
    public DatabaseReference getIssuesReference() {
        return databaseReference.child("issues");
    }

    // Get a specific issue
    public DatabaseReference getIssueReference(String issueId) {
        return databaseReference.child("issues").child(issueId);
    }

    // Save user data
    public void saveUser(User user) {
        Log.d(TAG, "Saving user: " + user.getUserId());
        databaseReference.child("users").child(user.getUserId()).setValue(user);
    }

    // Update issue status
    public void updateIssueStatus(String issueId, String status) {
        databaseReference.child("issues").child(issueId).child("status").setValue(status);
        databaseReference.child("issues").child(issueId).child("updatedAt").setValue(new java.util.Date().toString());
    }

    // Upvote an issue
    public void upvoteIssue(String issueId) {
        DatabaseReference issueRef = databaseReference.child("issues").child(issueId).child("upvotes");
        issueRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d(TAG, "Issue upvoted successfully");
                }
            }
        });
    }

    // Update statistics
    private void updateStats() {
        databaseReference.child("stats").child("totalIssues")
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                        if (committed) {
                            Log.d(TAG, "Stats updated successfully");
                        }
                    }
                });
    }

    // Debug method to check all issues
    public void debugAllIssues() {
        databaseReference.child("issues").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "=== ALL ISSUES IN DATABASE ===");
                Log.d(TAG, "Total count: " + snapshot.getChildrenCount());

                for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "Issue ID: " + issueSnapshot.getKey());
                    for (DataSnapshot field : issueSnapshot.getChildren()) {
                        Log.d(TAG, "  " + field.getKey() + ": " + field.getValue());
                    }
                    Log.d(TAG, "  ---");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Debug error: " + error.getMessage());
            }
        });
    }

    // Interface for callbacks
    public interface DatabaseCallback {
        void onSuccess(String issueId);
        void onFailure(Exception e);
    }
}