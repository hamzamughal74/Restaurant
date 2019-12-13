package com.digital.restaurant;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PreviewsAdapter extends RecyclerView.Adapter<PreviewsAdapter.ViewHolder> {

    private List<Uri> files;

    public PreviewsAdapter(List<Uri> files) {
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.li_preview_rv, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(holder.previewIv.getContext()).load(files.get(position)).into(holder.previewIv);
        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                files.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView previewIv;
        ImageView removeBtn;

        ViewHolder(View itemView) {
            super(itemView);
            previewIv = itemView.findViewById(R.id.previewIv);
            removeBtn = itemView.findViewById(R.id.removeBtn);
        }
    }
}
