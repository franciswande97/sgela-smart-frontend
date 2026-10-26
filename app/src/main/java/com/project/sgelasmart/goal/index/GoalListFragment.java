package com.project.sgelasmart.goal.index;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.ApplyTemplateRequest.TemplateTypeEnum;
import com.project.sgelasmart.api.model.GoalResponse;
import com.project.sgelasmart.api.service.GoalControllerApi;
import com.project.sgelasmart.core.api.ApiClient;
import com.project.sgelasmart.core.auth.SessionManager;
import com.project.sgelasmart.goal.detail.GoalDetailFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalListFragment extends Fragment {

    private static final String TAG = "GoalListFragment";

    private RecyclerView rvGoals;
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private GoalAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goal_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        rvGoals = view.findViewById(R.id.rv_goals);
        pbLoading = view.findViewById(R.id.pb_goals_loading);
        tvEmpty = view.findViewById(R.id.tv_goals_empty);

        adapter = new GoalAdapter();
        adapter.setOnGoalClickListener(goal -> navigateToDetail(goal, null));
        rvGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGoals.setAdapter(adapter);

        view.findViewById(R.id.btn_create_goal).setOnClickListener(v -> {
            CreateGoalBottomSheet sheet = new CreateGoalBottomSheet();
            sheet.setOnGoalCreatedListener((newGoal, chosenTemplate) -> {
                fetchGoals();
                navigateToDetail(newGoal, chosenTemplate != null ? chosenTemplate.getValue() : null);
            });
            sheet.show(getChildFragmentManager(), "CREATE_GOAL_SHEET");
        });

        fetchGoals();
    }

    private void navigateToDetail(GoalResponse goal, String templateStr) {
        if (goal == null || goal.getId() == null) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_container, GoalDetailFragment.newInstance(
                        goal.getId(),
                        goal.getTitle(),
                        goal.getDeadline() != null ? goal.getDeadline().toString() : "No deadline",
                        goal.getStatus() != null ? goal.getStatus().getValue() : "NOT_STARTED",
                        templateStr
                ))
                .addToBackStack(null)
                .commit();
    }

    private void fetchGoals() {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            tvEmpty.setText("User session invalid");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        GoalControllerApi goalApi = ApiClient.createService(GoalControllerApi.class);
        goalApi.getAllGoals(userId).enqueue(new Callback<List<GoalResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GoalResponse>> call, @NonNull Response<List<GoalResponse>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<GoalResponse> goals = response.body();
                    if (goals.isEmpty()) {
                        tvEmpty.setText("No goals found");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setGoals(goals);
                    }
                } else {
                    tvEmpty.setText("Failed to load goals (HTTP " + response.code() + ")");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GoalResponse>> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "Failed to retrieve goals", t);
                tvEmpty.setText("Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
