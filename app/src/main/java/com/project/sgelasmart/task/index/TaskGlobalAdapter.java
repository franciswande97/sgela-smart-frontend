package com.project.sgelasmart.task.index;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.ModelLocalTime;
import com.project.sgelasmart.api.model.TaskResponse;

import java.util.ArrayList;
import java.util.List;

public class TaskGlobalAdapter extends RecyclerView.Adapter<TaskGlobalAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(TaskResponse task);
    }

    private final List<TaskResponse> allTasks = new ArrayList<>();
    private final List<TaskResponse> displayedTasks = new ArrayList<>();
    private OnTaskClickListener listener;
    private String currentFilter = "ALL";

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<TaskResponse> list) {
        allTasks.clear();
        if (list != null) {
            allTasks.addAll(list);
        }
        applyFilter(currentFilter);
    }

    public void applyFilter(String filter) {
        this.currentFilter = filter;
        displayedTasks.clear();

        if ("ALL".equalsIgnoreCase(filter)) {
            displayedTasks.addAll(allTasks);
        } else {
            for (TaskResponse task : allTasks) {
                String state = task.getLifecycleState() != null ? task.getLifecycleState().getValue() : "TODO";
                if (filter.equalsIgnoreCase(state)) {
                    displayedTasks.add(task);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getTotalCount() {
        return allTasks.size();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_global, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = displayedTasks.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription() != null ? task.getDescription() : "");

        String type = task.getTaskType() != null ? task.getTaskType().getValue() : "TASK";
        holder.tvType.setText(type);
        if ("QUIZ".equalsIgnoreCase(type)) {
            holder.tvType.setTextColor(Color.parseColor("#A371F7"));
        } else if ("FOCUS_SESSION".equalsIgnoreCase(type)) {
            holder.tvType.setTextColor(Color.parseColor("#58A6FF"));
        } else if ("EXTERNAL_PROMPT".equalsIgnoreCase(type)) {
            holder.tvType.setTextColor(Color.parseColor("#D29922"));
        } else {
            holder.tvType.setTextColor(Color.parseColor("#3FB950"));
        }

        ModelLocalTime startTime = task.getStartTime();
        if (startTime != null && startTime.getHour() != null) {
            holder.tvTime.setText(String.format("%02d:%02d", startTime.getHour(), startTime.getMinute() != null ? startTime.getMinute() : 0));
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        Integer duration = task.getEstimatedDurationMinutes();
        holder.tvDuration.setText(duration != null ? "⏱ " + duration + "m" : "");

        holder.tvDue.setText(task.getDueDate() != null ? "Due: " + task.getDueDate().toString() : "");

        String state = task.getLifecycleState() != null ? task.getLifecycleState().getValue() : "TODO";
        holder.tvState.setText(state);
        if ("COMPLETED".equalsIgnoreCase(state)) {
            holder.tvState.setTextColor(Color.parseColor("#3FB950"));
        } else if ("IN_PROGRESS".equalsIgnoreCase(state)) {
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
        return displayedTasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvTime, tvDuration, tvTitle, tvDescription, tvDue, tvState;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_global_task_type);
            tvTime = itemView.findViewById(R.id.tv_global_task_time);
            tvDuration = itemView.findViewById(R.id.tv_global_task_duration);
            tvTitle = itemView.findViewById(R.id.tv_global_task_title);
            tvDescription = itemView.findViewById(R.id.tv_global_task_description);
            tvDue = itemView.findViewById(R.id.tv_global_task_due);
            tvState = itemView.findViewById(R.id.tv_global_task_state);
        }
    }
}
