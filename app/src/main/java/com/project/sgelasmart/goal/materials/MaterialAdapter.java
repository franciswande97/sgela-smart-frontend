package com.project.sgelasmart.goal.materials;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.GoalMaterialResponse;

import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    private final List<GoalMaterialResponse> materials = new ArrayList<>();

    public void setMaterials(List<GoalMaterialResponse> list) {
        materials.clear();
        if (list != null) {
            materials.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        GoalMaterialResponse item = materials.get(position);
        holder.tvFilename.setText(item.getFileName() != null ? item.getFileName() : "Document");
        holder.tvSnippet.setText(item.getExtractedContentSummary() != null ? item.getExtractedContentSummary() : "Corpus ready for AI synthesis");
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilename, tvSnippet;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilename = itemView.findViewById(R.id.tv_material_filename);
            tvSnippet = itemView.findViewById(R.id.tv_material_snippet);
        }
    }
}
