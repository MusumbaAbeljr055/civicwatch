package com.mihneacristian.civicwatch.presentation.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneacristian.civicwatch.databinding.ItemGalleryImageBinding;

import java.util.List;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {

    private List<Bitmap> images;
    private List<String> titles;

    public ImageGalleryAdapter(List<Bitmap> images, List<String> titles) {
        this.images = images;
        this.titles = titles;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryImageBinding binding = ItemGalleryImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Bitmap bitmap = images.get(position);
        holder.binding.imageView.setImageBitmap(bitmap);

        if (titles != null && position < titles.size()) {
            holder.binding.textTitle.setText(titles.get(position));
        } else {
            holder.binding.textTitle.setText("Image " + (position + 1));
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemGalleryImageBinding binding;

        ImageViewHolder(ItemGalleryImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}