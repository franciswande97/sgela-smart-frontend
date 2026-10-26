package com.project.sgelasmart.goal.detail;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.TaskResponse;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(TaskResponse task);
    }

    private final List<TaskResponse> tasks = new ArrayList<>();
    private OnTaskClickListener listener;

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<TaskResponse> list) {
        tasks.clear();
        if (list != null) {
            tasks.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription() != null ? task.getDescription() : "");

        String typeStr = task.getTaskType() != null ? task.getTaskType().getValue() : "TASK";
        holder.tvType.setText(typeStr);

        if ("QUIZ".equalsIgnoreCase(typeStr)) {
            holder.tvType.setTextColor(Color.parseColor("#A371F7"));
        } else if ("FOCUS_SESSION".equalsIgnoreCase(typeStr)) {
            holder.tvType.setTextColor(Color.parseColor("#58A6FF"));
        } else {
            holder.tvType.setTextColor(Color.parseColor("#3FB950"));
        }

        Integer duration = task.getEstimatedDurationMinutes();
        holder.tvDuration.setText(duration != null ? "⏱ " + duration + "m" : "");

        holder.tvDueDate.setText(task.getDueDate() != null ? "Due: " + task.getDueDate().toString() : "");

        String stateStr = task.getLifecycleState() != null ? task.getLifecycleState().getValue() : "TODO";
        holder.tvState.setText(stateStr);
        if ("COMPLETED".equalsIgnoreCase(stateStr)) {
            holder.tvState.setTextColor(Color.parseColor("#3FB950"));
        } else if ("IN_PROGRESS".equalsIgnoreCase(stateStr)) {
            holder.tvState.setTextColor(Color.parseColor("#58A6FF"));
        } else {
            holder.tvState.setTextColor(Color.parseColor("#D29922"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClick(task);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDuration, tvTitle, tvDescription, tvDueDate, tvState;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_task_type_badge);
            tvDuration = itemView.findViewById(R.id.tv_task_duration);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDescription = itemView.findViewById(R.id.tv_task_description);
            tvDueDate = itemView.findViewById(R.id.tv_task_due_date);
            tvState = itemView.findViewById(R.id.tv_task_state_badge);
        }
    }
}
