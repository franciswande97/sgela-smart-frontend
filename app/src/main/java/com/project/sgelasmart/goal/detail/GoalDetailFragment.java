package com.project.sgelasmart.goal.detail;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.ApplyTemplateRequest.TemplateTypeEnum;
import com.project.sgelasmart.api.model.GoalMaterialResponse;
import com.project.sgelasmart.api.model.TaskResponse;
import com.project.sgelasmart.task.detail.TaskDetailFragment;
import com.project.sgelasmart.api.service.GoalControllerApi;
import com.project.sgelasmart.api.service.TaskControllerApi;
import com.project.sgelasmart.core.api.ApiClient;
import com.project.sgelasmart.goal.materials.FileUploadHelper;
import com.project.sgelasmart.goal.materials.MaterialAdapter;
import com.project.sgelasmart.goal.materials.TextMaterialBottomSheet;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalDetailFragment extends Fragment {

    private static final String ARG_GOAL_ID = "arg_goal_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DEADLINE = "arg_deadline";
    private static final String ARG_STATUS = "arg_status";
    private static final String ARG_TEMPLATE = "arg_template";

    private Long goalId;
    private String goalTitle;
    private String deadlineStr;
    private String statusStr;
    private String templateStr;

    private MaterialAdapter materialAdapter;
    private View layoutEmptyMaterials;
    private RecyclerView rvMaterials;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadFile);

    public static GoalDetailFragment newInstance(Long id, String title, String deadline, String status, String template) {
        GoalDetailFragment fragment = new GoalDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_GOAL_ID, id);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DEADLINE, deadline);
        args.putString(ARG_STATUS, status);
        args.putString(ARG_TEMPLATE, template != null ? template : TemplateTypeEnum.EXAM_PREPARATION.getValue());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            goalId = getArguments().getLong(ARG_GOAL_ID);
            goalTitle = getArguments().getString(ARG_TITLE);
            deadlineStr = getArguments().getString(ARG_DEADLINE);
            statusStr = getArguments().getString(ARG_STATUS, "NOT_STARTED");
            templateStr = getArguments().getString(ARG_TEMPLATE, TemplateTypeEnum.EXAM_PREPARATION.getValue());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goal_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_detail_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvDeadline = view.findViewById(R.id.tv_detail_deadline);
        TextView tvStatus = view.findViewById(R.id.tv_detail_status_chip);
        FrameLayout stageContainer = view.findViewById(R.id.stage_container);

        tvTitle.setText(goalTitle);
        tvDeadline.setText("Deadline: " + deadlineStr);
        updateStatusChip(tvStatus, statusStr);

        renderStage(stageContainer);
    }

    private void updateStatusChip(TextView chip, String status) {
        chip.setText(status);
        if ("COMPLETED".equalsIgnoreCase(status)) {
            chip.setTextColor(Color.parseColor("#3FB950"));
        } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            chip.setTextColor(Color.parseColor("#58A6FF"));
        } else {
            chip.setTextColor(Color.parseColor("#D29922"));
        }
    }

    private void renderStage(FrameLayout container) {
        container.removeAllViews();
        if ("NOT_STARTED".equalsIgnoreCase(statusStr)) {
            View stageView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_stage_not_started, container, false);
            setupNotStartedStage(stageView);
            container.addView(stageView);
        } else {
            View stageView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_stage_in_progress, container, false);
            setupInProgressStage(stageView);
            container.addView(stageView);
        }
    }

    private void setupNotStartedStage(View view) {
        rvMaterials = view.findViewById(R.id.rv_materials);
        layoutEmptyMaterials = view.findViewById(R.id.layout_empty_materials);
        materialAdapter = new MaterialAdapter();
        rvMaterials.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMaterials.setAdapter(materialAdapter);

        view.findViewById(R.id.btn_upload_pdf).setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        view.findViewById(R.id.btn_add_text).setOnClickListener(v -> {
            TextMaterialBottomSheet sheet = TextMaterialBottomSheet.newInstance(goalId, this::fetchMaterials);
            sheet.show(getChildFragmentManager(), "TEXT_MATERIAL_SHEET");
        });

        view.findViewById(R.id.btn_generate_plan).setOnClickListener(v -> {
            ApplyTemplateBottomSheet sheet = ApplyTemplateBottomSheet.newInstance(goalId, templateStr, () -> {
                statusStr = "IN_PROGRESS";
                View root = getView();
                if (root != null) {
                    renderStage(root.findViewById(R.id.stage_container));
                    TextView tvStatus = root.findViewById(R.id.tv_detail_status_chip);
                    updateStatusChip(tvStatus, "IN_PROGRESS");
                }
            });
            sheet.show(getChildFragmentManager(), "APPLY_TEMPLATE_SHEET");
        });

        fetchMaterials();
    }

    private void setupInProgressStage(View view) {
        RecyclerView rvTasks = view.findViewById(R.id.rv_goal_tasks);
        ProgressBar pbLoading = view.findViewById(R.id.pb_tasks_loading);
        TextView tvEmpty = view.findViewById(R.id.tv_tasks_empty);

        TaskAdapter taskAdapter = new TaskAdapter();
        taskAdapter.setOnTaskClickListener(task -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_container, TaskDetailFragment.newInstance(task))
                    .addToBackStack(null)
                    .commit();
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);

        pbLoading.setVisibility(View.VISIBLE);
        TaskControllerApi taskApi = ApiClient.createService(TaskControllerApi.class);
        taskApi.getTasksByGoal(goalId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<TaskResponse>> call, @NonNull Response<List<TaskResponse>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskResponse> list = response.body();
                    taskAdapter.setTasks(list);
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TaskResponse>> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchMaterials() {
        GoalControllerApi api = ApiClient.createService(GoalControllerApi.class);
        api.getMaterials(goalId).enqueue(new Callback<List<GoalMaterialResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GoalMaterialResponse>> call, @NonNull Response<List<GoalMaterialResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GoalMaterialResponse> list = response.body();
                    materialAdapter.setMaterials(list);
                    boolean hasMaterials = !list.isEmpty();
                    if (rvMaterials != null) rvMaterials.setVisibility(hasMaterials ? View.VISIBLE : View.GONE);
                    if (layoutEmptyMaterials != null) layoutEmptyMaterials.setVisibility(hasMaterials ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GoalMaterialResponse>> call, @NonNull Throwable t) {
                // Keep empty state view
            }
        });
    }

    private void uploadFile(Uri uri) {
        if (uri == null) return;
        try {
            MultipartBody.Part filePart = FileUploadHelper.prepareFilePart(requireContext(), "file", uri);
            GoalControllerApi api = ApiClient.createService(GoalControllerApi.class);
            api.uploadMaterial(goalId, filePart).enqueue(new Callback<GoalMaterialResponse>() {
                @Override
                public void onResponse(@NonNull Call<GoalMaterialResponse> call, @NonNull Response<GoalMaterialResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "File attached successfully", Toast.LENGTH_SHORT).show();
                        fetchMaterials();
                    } else {
                        Toast.makeText(requireContext(), "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GoalMaterialResponse> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error preparing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
