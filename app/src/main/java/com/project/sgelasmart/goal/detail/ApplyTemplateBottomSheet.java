package com.project.sgelasmart.goal.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.ApplyTemplateRequest;
import com.project.sgelasmart.api.model.ApplyTemplateRequest.TemplateTypeEnum;
import com.project.sgelasmart.api.model.GoalTemplateResponse;
import com.project.sgelasmart.api.service.GoalControllerApi;
import com.project.sgelasmart.core.api.ApiClient;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyTemplateBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GOAL_ID = "arg_goal_id";
    private static final String ARG_TEMPLATE = "arg_template";

    private Long goalId;
    private String templateStr;
    private Runnable onSuccessListener;

    public static ApplyTemplateBottomSheet newInstance(Long goalId, String template, Runnable onSuccess) {
        ApplyTemplateBottomSheet sheet = new ApplyTemplateBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_GOAL_ID, goalId);
        args.putString(ARG_TEMPLATE, template != null ? template : TemplateTypeEnum.EXAM_PREPARATION.getValue());
        sheet.setArguments(args);
        sheet.onSuccessListener = onSuccess;
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            goalId = getArguments().getLong(ARG_GOAL_ID);
            templateStr = getArguments().getString(ARG_TEMPLATE, TemplateTypeEnum.EXAM_PREPARATION.getValue());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_apply_template, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_apply_title);
        EditText etTopics = view.findViewById(R.id.et_apply_topics);
        EditText etNotes = view.findViewById(R.id.et_apply_notes_snippet);
        ProgressBar pb = view.findViewById(R.id.pb_apply_template);
        View btnRun = view.findViewById(R.id.btn_run_apply_template);

        if ("ASSIGNMENT_ESSAY".equalsIgnoreCase(templateStr)) {
            tvTitle.setText("Generate Assignment Plan");
        } else if ("SOFTWARE_PRACTICAL".equalsIgnoreCase(templateStr)) {
            tvTitle.setText("Generate Practical Lab Plan");
        } else if ("CUSTOM_BARE".equalsIgnoreCase(templateStr)) {
            tvTitle.setText("Initialize Custom Goal");
        } else {
            tvTitle.setText("Generate Exam Prep Plan");
        }

        btnRun.setOnClickListener(v -> {
            pb.setVisibility(View.VISIBLE);
            btnRun.setEnabled(false);

            ApplyTemplateRequest request = new ApplyTemplateRequest();
            try {
                request.setTemplateType(TemplateTypeEnum.fromValue(templateStr));
            } catch (Exception e) {
                request.setTemplateType(TemplateTypeEnum.EXAM_PREPARATION);
            }

            String topicsRaw = etTopics.getText().toString().trim();
            if (!topicsRaw.isEmpty()) {
                request.setTopics(Arrays.asList(topicsRaw.split("\\s*,\\s*")));
            }
            request.setStudyNotesSnippet(etNotes.getText().toString().trim());

            GoalControllerApi api = ApiClient.createService(GoalControllerApi.class);
            api.applyTemplate(goalId, request).enqueue(new Callback<GoalTemplateResponse>() {
                @Override
                public void onResponse(@NonNull Call<GoalTemplateResponse> call, @NonNull Response<GoalTemplateResponse> response) {
                    pb.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        dismiss();
                        if (onSuccessListener != null) onSuccessListener.run();
                    } else {
                        btnRun.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed: HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GoalTemplateResponse> call, @NonNull Throwable t) {
                    pb.setVisibility(View.GONE);
                    // Server committed tasks but response JSON string formatting failed Gson mapping
                    if (t instanceof IllegalStateException || (t.getMessage() != null && t.getMessage().contains("Expected BEGIN_"))) {
                        dismiss();
                        if (onSuccessListener != null) onSuccessListener.run();
                    } else {
                        btnRun.setEnabled(true);
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
