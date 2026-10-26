package com.project.sgelasmart.task.detail.quiz;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.SanitizedQuizQuestionResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizQuestionAdapter extends RecyclerView.Adapter<QuizQuestionAdapter.QuestionViewHolder> {

    public interface OnOptionSelectedListener {
        void onOptionSelected(int questionIndex, int selectedOptionIndex);
    }

    private final List<SanitizedQuizQuestionResponse> questions = new ArrayList<>();
    private final Map<Integer, Integer> selections = new HashMap<>();
    private final OnOptionSelectedListener listener;

    public QuizQuestionAdapter(OnOptionSelectedListener listener) {
        this.listener = listener;
    }

    public void setQuestions(List<SanitizedQuizQuestionResponse> list) {
        questions.clear();
        if (list != null) questions.addAll(list);
        notifyDataSetChanged();
    }

    public Map<Integer, Integer> getSelections() {
        return selections;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_question_card, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        SanitizedQuizQuestionResponse item = questions.get(position);
        holder.tvQuestion.setText(item.getQuestion());

        List<String> options = item.getOptions() != null ? item.getOptions() : new ArrayList<>();
        Integer currentSelection = selections.get(position);

        LinearLayout[] tiles = {holder.tile0, holder.tile1, holder.tile2, holder.tile3};
        TextView[] texts = {holder.tvText0, holder.tvText1, holder.tvText2, holder.tvText3};
        TextView[] prefixes = {holder.tvPrefix0, holder.tvPrefix1, holder.tvPrefix2, holder.tvPrefix3};

        for (int i = 0; i < 4; i++) {
            final int optIdx = i;
            if (i < options.size()) {
                tiles[i].setVisibility(View.VISIBLE);
                texts[i].setText(options.get(i));

                boolean isSelected = currentSelection != null && currentSelection == optIdx;
                if (isSelected) {
                    tiles[i].setBackgroundResource(R.drawable.bg_filter_chip_selected);
                    prefixes[i].setTextColor(Color.parseColor("#58A6FF"));
                    texts[i].setTextColor(Color.parseColor("#58A6FF"));
                } else {
                    tiles[i].setBackgroundResource(R.drawable.bg_filter_chip_unselected);
                    prefixes[i].setTextColor(Color.parseColor("#8B949E"));
                    texts[i].setTextColor(Color.parseColor("#F0F6FC"));
                }

                tiles[i].setOnClickListener(v -> {
                    selections.put(position, optIdx);
                    notifyItemChanged(position);
                    if (listener != null) listener.onOptionSelected(position, optIdx);
                });
            } else {
                tiles[i].setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion;
        LinearLayout tile0, tile1, tile2, tile3;
        TextView tvPrefix0, tvPrefix1, tvPrefix2, tvPrefix3;
        TextView tvText0, tvText1, tvText2, tvText3;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_card_question_text);
            tile0 = itemView.findViewById(R.id.tile_option_0);
            tile1 = itemView.findViewById(R.id.tile_option_1);
            tile2 = itemView.findViewById(R.id.tile_option_2);
            tile3 = itemView.findViewById(R.id.tile_option_3);
            tvPrefix0 = itemView.findViewById(R.id.tv_option_prefix_0);
            tvPrefix1 = itemView.findViewById(R.id.tv_option_prefix_1);
            tvPrefix2 = itemView.findViewById(R.id.tv_option_prefix_2);
            tvPrefix3 = itemView.findViewById(R.id.tv_option_prefix_3);
            tvText0 = itemView.findViewById(R.id.tv_option_text_0);
            tvText1 = itemView.findViewById(R.id.tv_option_text_1);
            tvText2 = itemView.findViewById(R.id.tv_option_text_2);
            tvText3 = itemView.findViewById(R.id.tv_option_text_3);
        }
    }
}
