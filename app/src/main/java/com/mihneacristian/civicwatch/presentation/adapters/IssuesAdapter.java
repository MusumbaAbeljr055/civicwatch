package com.mihneacristian.civicwatch.presentation.adapters;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.databinding.ItemIssueBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.IssueViewHolder> {

    private List<Issue> issues = new ArrayList<>();
    private OnIssueClickListener listener;

    public interface OnIssueClickListener {
        void onIssueClick(Issue issue);
        void onIssueMenuClick(Issue issue, View view);
        void onUpvoteClick(Issue issue);
    }

    public void setOnIssueClickListener(OnIssueClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIssueBinding binding = ItemIssueBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new IssueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        Issue issue = issues.get(position);
        holder.bind(issue);
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
        notifyDataSetChanged();
    }

    class IssueViewHolder extends RecyclerView.ViewHolder {
        private final ItemIssueBinding binding;
        private Issue currentIssue; // Store current issue to avoid position issues

        IssueViewHolder(ItemIssueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (currentIssue != null && listener != null) {
                    listener.onIssueClick(currentIssue);
                }
            });

            binding.btnMenu.setOnClickListener(v -> {
                if (currentIssue != null) {
                    showPopupMenu(v, currentIssue);
                }
            });
        }

        void bind(Issue issue) {
            currentIssue = issue; // Store the current issue

            // Set basic text
            binding.issueTitle.setText(issue.getTitle());
            binding.issueDescription.setText(issue.getDescription());
            binding.issueCategory.setText(issue.getCategory());
            binding.issueSeverity.setText(issue.getSeverity());
            binding.issueStatus.setText(issue.getStatus());

            // Set upvotes count
            if (issue.getUpvotes() > 0) {
                binding.issueUpvotes.setText(String.valueOf(issue.getUpvotes()));
                binding.issueUpvotes.setVisibility(View.VISIBLE);
            } else {
                binding.issueUpvotes.setText("0");
                binding.issueUpvotes.setVisibility(View.VISIBLE);
            }

            // Format and set date
            String formattedDate = formatDate(issue.getCreatedAt());
            binding.issueDate.setText(formattedDate);

            // Set colors based on category, severity, and status
            setCategoryColor(issue.getCategory());
            setSeverityColor(issue.getSeverity());
            setStatusColor(issue.getStatus());
        }

        private void setCategoryColor(String category) {
            int colorRes;
            switch (category) {
                case "Pothole":
                    colorRes = R.color.category_pothole;
                    break;
                case "Graffiti":
                    colorRes = R.color.category_graffiti;
                    break;
                case "Litter":
                    colorRes = R.color.category_litter;
                    break;
                case "Illegal parking":
                    colorRes = R.color.category_parking;
                    break;
                case "Roadworks":
                    colorRes = R.color.category_roadworks;
                    break;
                case "Street lighting":
                    colorRes = R.color.category_lighting;
                    break;
                case "Illegal dumping":
                    colorRes = R.color.category_dumping;
                    break;
                case "Abandoned vehicle":
                    colorRes = R.color.category_vehicle;
                    break;
                case "Damaged tree":
                case "Fallen tree":
                case "Hanging branches":
                    colorRes = R.color.category_tree;
                    break;
                case "Worn out street sign":
                    colorRes = R.color.category_sign;
                    break;
                default:
                    colorRes = R.color.category_other;
                    break;
            }
            binding.issueCategory.setBackgroundColor(
                    itemView.getContext().getResources().getColor(colorRes)
            );
        }

        private void setSeverityColor(String severity) {
            int colorRes;
            switch (severity) {
                case "Minor":
                    colorRes = R.color.severity_minor;
                    break;
                case "Moderate":
                    colorRes = R.color.severity_moderate;
                    break;
                case "Major":
                    colorRes = R.color.severity_major;
                    break;
                case "Critical":
                    colorRes = R.color.severity_critical;
                    break;
                default:
                    colorRes = R.color.severity_moderate;
                    break;
            }
            binding.issueSeverity.setBackgroundColor(
                    itemView.getContext().getResources().getColor(colorRes)
            );
        }

        private void setStatusColor(String status) {
            int colorRes;
            switch (status) {
                case "PENDING":
                    colorRes = R.color.status_pending;
                    break;
                case "IN_PROGRESS":
                    colorRes = R.color.status_in_progress;
                    break;
                case "RESOLVED":
                    colorRes = R.color.status_resolved;
                    break;
                default:
                    colorRes = R.color.status_pending;
                    break;
            }
            binding.issueStatus.setBackgroundColor(
                    itemView.getContext().getResources().getColor(colorRes)
            );
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(dateString);
                if (date != null) {
                    long now = System.currentTimeMillis();
                    long time = date.getTime();

                    // Show relative time (e.g., "2 hours ago")
                    if (Math.abs(now - time) < DateUtils.DAY_IN_MILLIS * 7) {
                        return DateUtils.getRelativeTimeSpanString(
                                time,
                                now,
                                DateUtils.MINUTE_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_RELATIVE
                        ).toString();
                    } else {
                        // For older dates, show actual date
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        return outputFormat.format(date);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return dateString;
        }

        private void showPopupMenu(View view, Issue issue) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_issue_item, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.menu_view_details) {
                    if (listener != null) {
                        listener.onIssueClick(issue);
                    } else {
                        showToast("Listener not set");
                    }
                    return true;
                } else if (id == R.id.menu_upvote) {
                    if (listener != null) {
                        listener.onUpvoteClick(issue);
                    } else {
                        showToast("Please set listener first");
                    }
                    return true;
                } else if (id == R.id.menu_share) {
                    shareIssue(issue);
                    return true;
                } else if (id == R.id.menu_report) {
                    reportIssue(issue);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void showToast(String message) {
            Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
        }

        private void shareIssue(Issue issue) {
            String shareText = String.format(
                    "Check out this issue: %s\nType: %s\nSeverity: %s\nLocation: %s",
                    issue.getTitle(),
                    issue.getCategory(),
                    issue.getSeverity(),
                    issue.getAddress()
            );

            android.content.Intent shareIntent = new android.content.Intent();
            shareIntent.setAction(android.content.Intent.ACTION_SEND);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            shareIntent.setType("text/plain");

            itemView.getContext().startActivity(
                    android.content.Intent.createChooser(shareIntent, "Share Issue")
            );
        }

        private void reportIssue(Issue issue) {
            Toast.makeText(
                    itemView.getContext(),
                    "Report issue functionality coming soon!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}