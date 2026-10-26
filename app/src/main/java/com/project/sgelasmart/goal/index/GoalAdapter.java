package com.project.sgelasmart.goal.index;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.GoalResponse;

import java.util.ArrayList;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private final List<GoalResponse> goals = new ArrayList<>();
    private OnGoalClickListener clickListener;

    public interface OnGoalClickListener {
        void onGoalClick(GoalResponse goal);
    }

    public void setOnGoalClickListener(OnGoalClickListener listener) {
        this.clickListener = listener;
    }

    public void setGoals(List<GoalResponse> newGoals) {
        goals.clear();
        if (newGoals != null) {
            goals.addAll(newGoals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalResponse goal = goals.get(position);
        holder.tvTitle.setText(goal.getTitle());
        holder.tvDeadline.setText("Deadline: " + (goal.getDeadline() != null ? goal.getDeadline().toString() : "None"));
        holder.tvStatus.setText(goal.getStatus() != null ? goal.getStatus().getValue() : "NOT_STARTED");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onGoalClick(goal);
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDeadline, tvStatus;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_goal_title);
            tvDeadline = itemView.findViewById(R.id.tv_goal_deadline);
            tvStatus = itemView.findViewById(R.id.tv_goal_status);
        }
    }
}
