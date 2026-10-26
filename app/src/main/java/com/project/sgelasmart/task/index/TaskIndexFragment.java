package com.project.sgelasmart.task.index;
import com.project.sgelasmart.task.detail.TaskDetailFragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.TaskResponse;
import com.project.sgelasmart.api.service.TaskControllerApi;
import com.project.sgelasmart.core.api.ApiClient;
import com.project.sgelasmart.core.auth.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskIndexFragment extends Fragment {

    private RecyclerView rvTasks;
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private TextView tvCountPill;
    private TaskGlobalAdapter adapter;

    private TextView chipAll, chipTodo, chipInProgress, chipCompleted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_index, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTasks = view.findViewById(R.id.rv_global_tasks);
        pbLoading = view.findViewById(R.id.pb_tasks_loading);
        tvEmpty = view.findViewById(R.id.tv_tasks_empty);
        tvCountPill = view.findViewById(R.id.tv_tasks_count_pill);

        chipAll = view.findViewById(R.id.chip_filter_all);
        chipTodo = view.findViewById(R.id.chip_filter_todo);
        chipInProgress = view.findViewById(R.id.chip_filter_in_progress);
        chipCompleted = view.findViewById(R.id.chip_filter_completed);

        adapter = new TaskGlobalAdapter();
        adapter.setOnTaskClickListener(task -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_container, TaskDetailFragment.newInstance(task)).addToBackStack(null).commit();
        });

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);

        setupFilters();
        fetchTasks();
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> selectFilter("ALL", chipAll));
        chipTodo.setOnClickListener(v -> selectFilter("TODO", chipTodo));
        chipInProgress.setOnClickListener(v -> selectFilter("IN_PROGRESS", chipInProgress));
        chipCompleted.setOnClickListener(v -> selectFilter("COMPLETED", chipCompleted));
    }

    private void selectFilter(String filter, TextView selectedChip) {
        resetChip(chipAll);
        resetChip(chipTodo);
        resetChip(chipInProgress);
        resetChip(chipCompleted);

        selectedChip.setBackgroundResource(R.drawable.bg_filter_chip_selected);
        selectedChip.setTextColor(Color.parseColor("#58A6FF"));

        adapter.applyFilter(filter);
        tvEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void resetChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_filter_chip_unselected);
        chip.setTextColor(Color.parseColor("#8B949E"));
    }

    private void fetchTasks() {
        SessionManager sessionManager = new SessionManager(requireContext());
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            tvEmpty.setText("Invalid user session");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        TaskControllerApi api = ApiClient.createService(TaskControllerApi.class);
        api.getAllTasks(userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<TaskResponse>> call, @NonNull Response<List<TaskResponse>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskResponse> list = response.body();
                    adapter.setTasks(list);
                    tvCountPill.setText(adapter.getTotalCount() + " Tasks");
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setText("Failed to retrieve agenda: " + response.code());
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TaskResponse>> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                tvEmpty.setText("Error: " + t.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
