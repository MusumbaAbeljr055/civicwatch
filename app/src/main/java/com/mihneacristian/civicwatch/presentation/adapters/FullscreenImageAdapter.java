package com.mihneacristian.civicwatch.presentation.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.ItemFullscreenImageBinding;

import java.util.List;

public class FullscreenImageAdapter extends RecyclerView.Adapter<FullscreenImageAdapter.FullscreenViewHolder> {

    private List<String> imageUrls;

    public FullscreenImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public FullscreenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFullscreenImageBinding binding = ItemFullscreenImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FullscreenViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FullscreenViewHolder holder, int position) {
        String imageData = imageUrls.get(position);

        // Check if it's Base64 or URL
        if (imageData.startsWith("data:image") || imageData.contains(",")) {
            // Handle Base64
            String base64String = imageData;
            if (base64String.contains(",")) {
                base64String = base64String.substring(base64String.indexOf(",") + 1);
            }
            try {
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.binding.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.binding.imageView.setImageResource(R.drawable.ic_default_image);
            }
        } else {
            // Handle URL with Glide
            // You can add Glide here if needed
            holder.binding.imageView.setImageResource(R.drawable.ic_default_image);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class FullscreenViewHolder extends RecyclerView.ViewHolder {
        private final ItemFullscreenImageBinding binding;

        FullscreenViewHolder(ItemFullscreenImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}