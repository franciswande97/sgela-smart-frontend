package com.project.sgelasmart.task.detail;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.AnswerItem;
import com.project.sgelasmart.api.model.QuizSubmissionRequest;
import com.project.sgelasmart.api.model.QuizSubmissionResponse;
import com.project.sgelasmart.api.model.SanitizedQuizQuestionResponse;
import com.project.sgelasmart.api.model.TaskRequest;
import com.project.sgelasmart.api.model.TaskResponse;
import com.project.sgelasmart.api.service.TaskControllerApi;
import com.project.sgelasmart.core.api.ApiClient;
import com.project.sgelasmart.task.detail.quiz.QuizQuestionAdapter;
import com.project.sgelasmart.task.detail.quiz.QuizReviewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailFragment extends Fragment {

    private static final String ARG_TASK_ID = "arg_task_id";
    private static final String ARG_GOAL_ID = "arg_goal_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_STATE = "arg_state";
    private static final String ARG_DURATION = "arg_duration";

    private Long taskId;
    private Long goalId;
    private String taskTitle;
    private String taskDesc;
    private String taskType;
    private String currentState;
    private int durationMinutes;

    private TextView tvTitle, tvTypePill, tvStatePill;
    private FrameLayout stageContainer;

    public static TaskDetailFragment newInstance(TaskResponse task) {
        TaskDetailFragment f = new TaskDetailFragment();
        Bundle args = new Bundle();
        if (task != null) {
            args.putLong(ARG_TASK_ID, task.getId() != null ? task.getId() : 0L);
            args.putLong(ARG_GOAL_ID, task.getGoalId() != null ? task.getGoalId() : 0L);
            args.putString(ARG_TITLE, task.getTitle());
            args.putString(ARG_DESCRIPTION, task.getDescription());
            args.putString(ARG_TYPE, task.getTaskType() != null ? task.getTaskType().getValue() : "QUIZ");

            String state = task.getLifecycleState() != null ? task.getLifecycleState().getValue() : "TODO";
            if ("IN_PROGRESS".equalsIgnoreCase(state)) {
                state = "TODO";
            }
            args.putString(ARG_STATE, state);
            args.putInt(ARG_DURATION, task.getEstimatedDurationMinutes() != null ? task.getEstimatedDurationMinutes() : 20);
        }
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getLong(ARG_TASK_ID);
            goalId = getArguments().getLong(ARG_GOAL_ID);
            taskTitle = getArguments().getString(ARG_TITLE, "Task Detail");
            taskDesc = getArguments().getString(ARG_DESCRIPTION, "");
            taskType = getArguments().getString(ARG_TYPE, "QUIZ");
            currentState = getArguments().getString(ARG_STATE, "TODO");
            durationMinutes = getArguments().getInt(ARG_DURATION, 20);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_task_detail_back);
        tvTitle = view.findViewById(R.id.tv_task_detail_title);
        tvTypePill = view.findViewById(R.id.tv_task_detail_type_pill);
        tvStatePill = view.findViewById(R.id.tv_task_detail_state_pill);
        stageContainer = view.findViewById(R.id.fl_quiz_stage_container);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        tvTitle.setText(taskTitle);
        tvTypePill.setText(taskType);
        updateStateHeader(currentState);

        renderCurrentStage();
    }

    private void updateStateHeader(String state) {
        this.currentState = state;
        tvStatePill.setText(state);
        if ("COMPLETED".equalsIgnoreCase(state)) {
            tvStatePill.setTextColor(Color.parseColor("#3FB950"));
        } else if ("IN_PROGRESS".equalsIgnoreCase(state)) {
            tvStatePill.setTextColor(Color.parseColor("#58A6FF"));
        } else {
            tvStatePill.setTextColor(Color.parseColor("#D29922"));
        }
    }

    private void renderCurrentStage() {
        stageContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        if ("COMPLETED".equalsIgnoreCase(currentState)) {
            renderCompletedStage(inflater);
        } else if ("IN_PROGRESS".equalsIgnoreCase(currentState)) {
            renderInProgressStage(inflater);
        } else {
            renderTodoStage(inflater);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // STAGE 1: TODO
    // ─────────────────────────────────────────────────────────────
    private void renderTodoStage(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.layout_quiz_todo, stageContainer, false);
        stageContainer.addView(view);

        TextView tvDesc = view.findViewById(R.id.tv_quiz_todo_description);
        TextView tvDuration = view.findViewById(R.id.tv_quiz_todo_duration);
        ProgressBar pbStart = view.findViewById(R.id.pb_quiz_start);
        MaterialButton btnStart = view.findViewById(R.id.btn_quiz_start);

        tvDesc.setText(taskDesc != null && !taskDesc.isEmpty() ? taskDesc : "Prepare to test your knowledge against active recall prompts.");
        tvDuration.setText(durationMinutes + " mins");

        btnStart.setOnClickListener(v -> {
            pbStart.setVisibility(View.VISIBLE);
            btnStart.setEnabled(false);

            TaskRequest req = new TaskRequest();
            req.setTitle(taskTitle);
            req.setDescription(taskDesc);
            req.setEstimatedDurationMinutes(durationMinutes);
            try {
                req.setTaskType(TaskRequest.TaskTypeEnum.fromValue(taskType));
                req.setLifecycleState(TaskRequest.LifecycleStateEnum.IN_PROGRESS);
            } catch (Exception ignored) {}

            TaskControllerApi api = ApiClient.createService(TaskControllerApi.class);
            api.updateTask(taskId, req).enqueue(new Callback<TaskResponse>() {
                @Override
                public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                    pbStart.setVisibility(View.GONE);
                    updateStateHeader("IN_PROGRESS");
                    renderCurrentStage();
                }

                @Override
                public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                    pbStart.setVisibility(View.GONE);
                    updateStateHeader("IN_PROGRESS");
                    renderCurrentStage();
                }
            });
        });
    }

    // ─────────────────────────────────────────────────────────────
    // STAGE 2: IN_PROGRESS
    // ─────────────────────────────────────────────────────────────
    private void renderInProgressStage(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.layout_quiz_in_progress, stageContainer, false);
        stageContainer.addView(view);

        TextView tvStep = view.findViewById(R.id.tv_quiz_step_counter);
        TextView tvPct = view.findViewById(R.id.tv_quiz_progress_pct);
        LinearProgressIndicator lpi = view.findViewById(R.id.lpi_quiz_progress);
        ProgressBar pb = view.findViewById(R.id.pb_quiz_questions_loading);
        ViewPager2 vpDeck = view.findViewById(R.id.vp_quiz_deck);
        MaterialButton btnPrev = view.findViewById(R.id.btn_quiz_prev);
        MaterialButton btnNext = view.findViewById(R.id.btn_quiz_next);

        vpDeck.setUserInputEnabled(false);

        QuizQuestionAdapter adapter = new QuizQuestionAdapter((qIdx, optIdx) -> {});
        vpDeck.setAdapter(adapter);

        pb.setVisibility(View.VISIBLE);
        TaskControllerApi api = ApiClient.createService(TaskControllerApi.class);
        api.getQuizQuestions(taskId).enqueue(new Callback<List<SanitizedQuizQuestionResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<SanitizedQuizQuestionResponse>> call, @NonNull Response<List<SanitizedQuizQuestionResponse>> response) {
                pb.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<SanitizedQuizQuestionResponse> list = response.body();
                    adapter.setQuestions(list);
                    updateProgressUI(0, list.size(), tvStep, tvPct, lpi, btnPrev, btnNext);
                } else {
                    Toast.makeText(requireContext(), "Failed to load questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SanitizedQuizQuestionResponse>> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        vpDeck.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateProgressUI(position, adapter.getItemCount(), tvStep, tvPct, lpi, btnPrev, btnNext);
            }
        });

        btnPrev.setOnClickListener(v -> {
            int current = vpDeck.getCurrentItem();
            if (current > 0) vpDeck.setCurrentItem(current - 1, true);
        });

        btnNext.setOnClickListener(v -> {
            int current = vpDeck.getCurrentItem();
            int total = adapter.getItemCount();

            if (current < total - 1) {
                vpDeck.setCurrentItem(current + 1, true);
            } else {
                submitQuiz(adapter, pb, btnNext);
            }
        });
    }

    private void updateProgressUI(int position, int total, TextView tvStep, TextView tvPct,
                                  LinearProgressIndicator lpi, MaterialButton btnPrev, MaterialButton btnNext) {
        if (total == 0) return;
        tvStep.setText("Question " + (position + 1) + " of " + total);
        int percent = (int) (((position + 1) / (float) total) * 100);
        tvPct.setText(percent + "%");
        lpi.setProgress(percent);

        btnPrev.setEnabled(position > 0);
        if (position == total - 1) {
            btnNext.setText("Submit & Grade");
            btnNext.setBackgroundColor(Color.parseColor("#1F6FEB"));
        } else {
            btnNext.setText("Next");
            btnNext.setBackgroundColor(Color.parseColor("#238636"));
        }
    }

    private void submitQuiz(QuizQuestionAdapter adapter, ProgressBar pb, MaterialButton btnNext) {
        pb.setVisibility(View.VISIBLE);
        btnNext.setEnabled(false);

        Map<Integer, Integer> selections = adapter.getSelections();
        QuizSubmissionRequest submissionRequest = new QuizSubmissionRequest();
        List<AnswerItem> answers = new ArrayList<>();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            AnswerItem item = new AnswerItem();
            item.setQuestionIndex(i);
            item.setSelectedOptionIndex(selections.getOrDefault(i, 0));
            answers.add(item);
        }
        submissionRequest.setAnswers(answers);

        TaskControllerApi api = ApiClient.createService(TaskControllerApi.class);
        api.submitQuiz(taskId, submissionRequest).enqueue(new Callback<QuizSubmissionResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizSubmissionResponse> call, @NonNull Response<QuizSubmissionResponse> response) {
                pb.setVisibility(View.GONE);
                updateStateHeader("COMPLETED");
                renderCompletedStageDirect(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<QuizSubmissionResponse> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                updateStateHeader("COMPLETED");
                renderCompletedStageDirect(null);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // STAGE 3: COMPLETED
    // ─────────────────────────────────────────────────────────────
    private void renderCompletedStage(LayoutInflater inflater) {
        renderCompletedStageDirect(null);
    }

    private void renderCompletedStageDirect(QuizSubmissionResponse result) {
        stageContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.layout_quiz_completed, stageContainer, false);
        stageContainer.addView(view);

        TextView tvScorePct = view.findViewById(R.id.tv_quiz_completed_score_pct);
        TextView tvFraction = view.findViewById(R.id.tv_quiz_completed_fraction);
        TextView tvVerdict = view.findViewById(R.id.tv_quiz_completed_verdict_pill);
        RecyclerView rvReview = view.findViewById(R.id.rv_quiz_review);
        MaterialButton btnDone = view.findViewById(R.id.btn_quiz_done);
        MaterialButton btnRetake = view.findViewById(R.id.btn_quiz_regenerate);

        QuizReviewAdapter reviewAdapter = new QuizReviewAdapter();
        rvReview.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReview.setAdapter(reviewAdapter);

        if (result != null) {
            tvScorePct.setText(String.format("%.0f%%", result.getPercentage() != null ? result.getPercentage() : 0.0));
            tvFraction.setText(result.getScore() + " of " + result.getTotalQuestions() + " correct");
            boolean passed = Boolean.TRUE.equals(result.getPassed());
            tvVerdict.setText(passed ? "PASSED" : "NEEDS PRACTICE");
            tvVerdict.setTextColor(passed ? Color.parseColor("#3FB950") : Color.parseColor("#D29922"));

            if (result.getReview() != null) {
                reviewAdapter.setReviews(result.getReview());
            }
        } else {
            tvScorePct.setText("QUIZ DONE");
            tvFraction.setText("Results recorded on server");
            tvVerdict.setText("COMPLETED");
        }

        btnDone.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnRetake.setOnClickListener(v -> {
            btnRetake.setEnabled(false);

            TaskRequest req = new TaskRequest();
            req.setTitle(taskTitle);
            req.setDescription(taskDesc);
            req.setEstimatedDurationMinutes(durationMinutes);
            try {
                req.setTaskType(TaskRequest.TaskTypeEnum.fromValue(taskType));
                req.setLifecycleState(TaskRequest.LifecycleStateEnum.IN_PROGRESS);
            } catch (Exception ignored) {}

            TaskControllerApi api = ApiClient.createService(TaskControllerApi.class);
            api.updateTask(taskId, req).enqueue(new Callback<TaskResponse>() {
                @Override
                public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                    updateStateHeader("IN_PROGRESS");
                    renderCurrentStage();
                }

                @Override
                public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                    updateStateHeader("IN_PROGRESS");
                    renderCurrentStage();
                }
            });
        });
    }
}
