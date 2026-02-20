package com.mihneacristian.civicwatch.presentation.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.databinding.ItemIssueBinding;
import com.mihneacristian.civicwatch.presentation.activities.ImageGalleryActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.IssueViewHolder> {

    private List<Issue> issues = new ArrayList<>();
    private OnIssueClickListener listener;
    private static final String TAG = "IssuesAdapter";

    public interface OnIssueClickListener {
        void onIssueClick(Issue issue);
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
        private Issue currentIssue;

        IssueViewHolder(ItemIssueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                if (currentIssue != null && listener != null) {
                    listener.onIssueClick(currentIssue);
                }
            });

            binding.btnUpvote.setOnClickListener(v -> {
                if (currentIssue != null && listener != null) {
                    listener.onUpvoteClick(currentIssue);
                }
            });

            binding.btnMenu.setOnClickListener(v -> {
                if (currentIssue != null) {
                    showPopupMenu(v, currentIssue);
                }
            });

            // Add click listener to the image to open gallery
            binding.issueImage.setOnClickListener(v -> {
                if (currentIssue != null) {
                    openImageGallery();
                }
            });
        }

        private void openImageGallery() {
            if (currentIssue == null) return;

            Log.d(TAG, "Opening gallery for issue: " + currentIssue.getTitle());

            Intent intent = new Intent(itemView.getContext(), ImageGalleryActivity.class);

            ArrayList<String> allImages = new ArrayList<>();
            ArrayList<String> allTitles = new ArrayList<>();

            // Add the current image
            if (currentIssue.getPhotoBase64() != null && !currentIssue.getPhotoBase64().isEmpty()) {
                allImages.add(currentIssue.getPhotoBase64());
                allTitles.add(currentIssue.getTitle());

                Log.d(TAG, "Added image to gallery, Base64 length: " + currentIssue.getPhotoBase64().length());
            } else {
                Log.d(TAG, "No image data found for this issue");
                Toast.makeText(itemView.getContext(),
                        "No image available for this issue",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // For future enhancement: You could add multiple images here
            // if you implement multi-image support per issue

            intent.putStringArrayListExtra("all_images", allImages);
            intent.putStringArrayListExtra("all_titles", allTitles);
            intent.putExtra("position", 0);

            itemView.getContext().startActivity(intent);
        }

        void bind(Issue issue) {
            currentIssue = issue;

            // Set title
            binding.issueTitle.setText(issue.getTitle());

            // Load image from Base64 or show default
            loadImageFromBase64(issue.getPhotoBase64());

            // Set date
            String formattedDate = formatDate(issue.getCreatedAt());
            binding.issueDateAdded.setText(formattedDate);

            // Set location
            if (issue.getLatitude() != 0 && issue.getLongitude() != 0) {
                String locationText = String.format(Locale.getDefault(),
                        "%.2f, %.2f", issue.getLatitude(), issue.getLongitude());
                binding.issueLocationText.setText(locationText);
            } else {
                binding.issueLocationText.setText("Location not available");
            }
        }

        private void loadImageFromBase64(String base64String) {
            if (base64String == null || base64String.isEmpty()) {
                Log.d(TAG, "No image data for issue: " + (currentIssue != null ? currentIssue.getTitle() : "unknown"));
                binding.issueImage.setImageResource(R.drawable.ic_default_image);
                binding.issueImage.setBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.background_gray));
                binding.issueImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                return;
            }

            try {
                Log.d(TAG, "Attempting to load image, Base64 length: " + base64String.length());

                String cleanBase64 = base64String;
                if (cleanBase64.contains(",")) {
                    cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                }

                cleanBase64 = cleanBase64.replaceAll("\\s", "");
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                if (bitmap != null) {
                    binding.issueImage.setImageBitmap(bitmap);
                    binding.issueImage.setBackground(null);
                    binding.issueImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    // Make image clickable only when there's actual image
                    binding.issueImage.setClickable(true);
                    binding.issueImage.setAlpha(1.0f);

                    Log.d(TAG, "Image loaded successfully for issue: " + currentIssue.getTitle());
                } else {
                    Log.e(TAG, "Failed to decode bitmap for issue: " + currentIssue.getTitle());
                    binding.issueImage.setImageResource(R.drawable.ic_default_image);
                    binding.issueImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    binding.issueImage.setClickable(false);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Base64 decoding error: " + e.getMessage());
                binding.issueImage.setImageResource(R.drawable.ic_default_image);
                binding.issueImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                binding.issueImage.setClickable(false);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from Base64", e);
                binding.issueImage.setImageResource(R.drawable.ic_default_image);
                binding.issueImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                binding.issueImage.setClickable(false);
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "Unknown date";
            }

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(dateString);

                if (date == null) {
                    SimpleDateFormat oldFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                    date = oldFormat.parse(dateString);
                }

                if (date != null) {
                    long now = System.currentTimeMillis();
                    long time = date.getTime();

                    if (Math.abs(now - time) < DateUtils.DAY_IN_MILLIS * 7) {
                        return DateUtils.getRelativeTimeSpanString(
                                time,
                                now,
                                DateUtils.MINUTE_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_RELATIVE
                        ).toString();
                    } else {
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        return outputFormat.format(date);
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + dateString, e);
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
                    }
                    return true;
                } else if (id == R.id.menu_upvote) {
                    if (listener != null) {
                        listener.onUpvoteClick(issue);
                    }
                    return true;
                } else if (id == R.id.menu_share) {
                    shareIssue(issue);
                    return true;
                } else if (id == R.id.menu_view_image) {
                    openImageGallery();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void shareIssue(Issue issue) {
            String shareText = String.format(
                    "Check out this issue: %s\nReported on: %s\nLocation: %.2f, %.2f",
                    issue.getTitle(),
                    formatDate(issue.getCreatedAt()),
                    issue.getLatitude(),
                    issue.getLongitude()
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setType("text/plain");

            itemView.getContext().startActivity(
                    Intent.createChooser(shareIntent, "Share Issue")
            );
        }
    }
}