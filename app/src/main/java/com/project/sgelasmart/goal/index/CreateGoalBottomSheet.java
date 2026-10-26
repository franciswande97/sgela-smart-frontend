package com.project.sgelasmart.goal.index;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.ApplyTemplateRequest.TemplateTypeEnum;
import com.project.sgelasmart.api.model.GoalRequest;
import com.project.sgelasmart.api.model.GoalResponse;
import com.project.sgelasmart.api.service.GoalControllerApi;
import com.project.sgelasmart.core.api.ApiClient;
import com.project.sgelasmart.core.auth.SessionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGoalBottomSheet extends BottomSheetDialogFragment {

    private LocalDate selectedDeadline = null;
    private TemplateTypeEnum selectedTemplate = TemplateTypeEnum.EXAM_PREPARATION;
    private OnGoalCreatedListener listener;

    private LinearLayout cardExam, cardEssay, cardSoftware, cardCustom;
    private TextView tvExamTitle, tvEssayTitle, tvSoftwareTitle, tvCustomTitle;

    public interface OnGoalCreatedListener {
        void onGoalCreated(GoalResponse goal, TemplateTypeEnum chosenTemplate);
    }

    public void setOnGoalCreatedListener(OnGoalCreatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_create_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etTitle = view.findViewById(R.id.et_goal_title);
        TextView tvDeadline = view.findViewById(R.id.tv_pick_deadline);
        ProgressBar pb = view.findViewById(R.id.pb_create_goal);
        View btnSubmit = view.findViewById(R.id.btn_submit_create_goal);

        cardExam = view.findViewById(R.id.card_tpl_exam);
        cardEssay = view.findViewById(R.id.card_tpl_essay);
        cardSoftware = view.findViewById(R.id.card_tpl_software);
        cardCustom = view.findViewById(R.id.card_tpl_custom);

        tvExamTitle = view.findViewById(R.id.tv_tpl_exam_title);
        tvEssayTitle = view.findViewById(R.id.tv_tpl_essay_title);
        tvSoftwareTitle = view.findViewById(R.id.tv_tpl_software_title);
        tvCustomTitle = view.findViewById(R.id.tv_tpl_custom_title);

        cardExam.setOnClickListener(v -> selectTemplate(TemplateTypeEnum.EXAM_PREPARATION));
        cardEssay.setOnClickListener(v -> selectTemplate(TemplateTypeEnum.ASSIGNMENT_ESSAY));
        cardSoftware.setOnClickListener(v -> selectTemplate(TemplateTypeEnum.SOFTWARE_PRACTICAL));
        cardCustom.setOnClickListener(v -> selectTemplate(TemplateTypeEnum.CUSTOM_BARE));

        tvDeadline.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Target Deadline")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDeadline = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate();
                tvDeadline.setText(selectedDeadline.format(DateTimeFormatter.ISO_LOCAL_DATE));
            });
            picker.show(getParentFragmentManager(), "DEADLINE_PICKER");
        });

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Title cannot be blank", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedDeadline == null) {
                Toast.makeText(requireContext(), "Please select a deadline", Toast.LENGTH_SHORT).show();
                return;
            }

            SessionManager sessionManager = new SessionManager(requireContext());
            Long userId = sessionManager.getUserId();
            if (userId == null) return;

            pb.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);

            GoalRequest request = new GoalRequest();
            request.setTitle(title);
            request.setDeadline(selectedDeadline);

            GoalControllerApi api = ApiClient.createService(GoalControllerApi.class);
            api.createGoal(userId, request).enqueue(new Callback<GoalResponse>() {
                @Override
                public void onResponse(@NonNull Call<GoalResponse> call, @NonNull Response<GoalResponse> response) {
                    pb.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        if (listener != null) listener.onGoalCreated(response.body(), selectedTemplate);
                        dismiss();
                    } else {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(requireContext(), "Creation failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GoalResponse> call, @NonNull Throwable t) {
                    pb.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void selectTemplate(TemplateTypeEnum template) {
        selectedTemplate = template;

        resetCard(cardExam, tvExamTitle);
        resetCard(cardEssay, tvEssayTitle);
        resetCard(cardSoftware, tvSoftwareTitle);
        resetCard(cardCustom, tvCustomTitle);

        switch (template) {
            case EXAM_PREPARATION:
                highlightCard(cardExam, tvExamTitle);
                break;
            case ASSIGNMENT_ESSAY:
                highlightCard(cardEssay, tvEssayTitle);
                break;
            case SOFTWARE_PRACTICAL:
                highlightCard(cardSoftware, tvSoftwareTitle);
                break;
            case CUSTOM_BARE:
                highlightCard(cardCustom, tvCustomTitle);
                break;
        }
    }

    private void resetCard(LinearLayout card, TextView title) {
        card.setBackgroundResource(R.drawable.bg_template_unselected);
        title.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void highlightCard(LinearLayout card, TextView title) {
        card.setBackgroundResource(R.drawable.bg_template_selected);
        title.setTextColor(Color.parseColor("#58A6FF"));
    }
}
