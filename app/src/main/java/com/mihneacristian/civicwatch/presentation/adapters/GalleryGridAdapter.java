package com.mihneacristian.civicwatch.presentation.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneacristian.civicwatch.databinding.ItemGalleryGridBinding;

import java.util.List;

public class GalleryGridAdapter extends RecyclerView.Adapter<GalleryGridAdapter.GridViewHolder> {

    private List<Bitmap> images;
    private List<String> titles;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public GalleryGridAdapter(List<Bitmap> images, List<String> titles, OnImageClickListener listener) {
        this.images = images;
        this.titles = titles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryGridBinding binding = ItemGalleryGridBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GridViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        Bitmap bitmap = images.get(position);
        holder.binding.imageView.setImageBitmap(bitmap);

        if (titles != null && position < titles.size()) {
            holder.binding.textTitle.setText(titles.get(position));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {
        private final ItemGalleryGridBinding binding;

        GridViewHolder(ItemGalleryGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}