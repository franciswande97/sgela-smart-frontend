package com.project.sgelasmart.task.detail.quiz;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.QuestionReviewItem;

import java.util.ArrayList;
import java.util.List;

public class QuizReviewAdapter extends RecyclerView.Adapter<QuizReviewAdapter.ReviewViewHolder> {

    private final List<QuestionReviewItem> reviews = new ArrayList<>();

    public void setReviews(List<QuestionReviewItem> list) {
        reviews.clear();
        if (list != null) reviews.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        QuestionReviewItem item = reviews.get(position);

        int qIdx = item.getQuestionIndex() != null ? item.getQuestionIndex() + 1 : position + 1;
        holder.tvIdx.setText("Question " + qIdx);

        holder.tvQuestion.setText(item.getQuestion());
        holder.tvExplanation.setText(item.getExplanation());

        boolean isCorrect = Boolean.TRUE.equals(item.getIsCorrect());
        if (isCorrect) {
            holder.tvStatus.setText("CORRECT");
            holder.tvStatus.setTextColor(Color.parseColor("#3FB950"));
        } else {
            holder.tvStatus.setText("INCORRECT");
            holder.tvStatus.setTextColor(Color.parseColor("#F85149"));
        }

        List<String> options = item.getOptions();
        int userChoice = item.getSelectedOptionIndex() != null ? item.getSelectedOptionIndex() : -1;
        int correctChoice = item.getCorrectAnswerIndex() != null ? item.getCorrectAnswerIndex() : -1;

        if (options != null && userChoice >= 0 && userChoice < options.size()) {
            holder.tvUserChoice.setText("Your answer: " + options.get(userChoice));
        } else {
            holder.tvUserChoice.setText("Your answer: Unanswered");
        }

        if (!isCorrect && options != null && correctChoice >= 0 && correctChoice < options.size()) {
            holder.tvCorrectChoice.setVisibility(View.VISIBLE);
            holder.tvCorrectChoice.setText("Correct answer: " + options.get(correctChoice));
        } else {
            holder.tvCorrectChoice.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvIdx, tvQuestion, tvUserChoice, tvCorrectChoice, tvExplanation;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_review_status_pill);
            tvIdx = itemView.findViewById(R.id.tv_review_question_idx);
            tvQuestion = itemView.findViewById(R.id.tv_review_question);
            tvUserChoice = itemView.findViewById(R.id.tv_review_user_choice);
            tvCorrectChoice = itemView.findViewById(R.id.tv_review_correct_choice);
            tvExplanation = itemView.findViewById(R.id.tv_review_explanation);
        }
    }
}
