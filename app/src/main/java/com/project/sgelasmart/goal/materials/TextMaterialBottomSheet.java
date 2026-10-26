package com.project.sgelasmart.goal.materials;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.GoalMaterialResponse;
import com.project.sgelasmart.api.model.MaterialTextRequest;
import com.project.sgelasmart.api.service.GoalControllerApi;
import com.project.sgelasmart.core.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TextMaterialBottomSheet extends BottomSheetDialogFragment {

    private Long goalId;
    private Runnable onComplete;

    public static TextMaterialBottomSheet newInstance(Long goalId, Runnable onComplete) {
        TextMaterialBottomSheet sheet = new TextMaterialBottomSheet();
        sheet.goalId = goalId;
        sheet.onComplete = onComplete;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_text_material, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etFileName = view.findViewById(R.id.et_text_material_name);
        EditText etContent = view.findViewById(R.id.et_text_material_content);
        ProgressBar pbLoading = view.findViewById(R.id.pb_text_material);
        View btnSave = view.findViewById(R.id.btn_save_text_material);

        btnSave.setOnClickListener(v -> {
            String name = etFileName.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (name.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Title and text content required", Toast.LENGTH_SHORT).show();
                return;
            }

            pbLoading.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            MaterialTextRequest request = new MaterialTextRequest();
            request.setTitle(name);
            request.setContent(content);

            GoalControllerApi api = ApiClient.createService(GoalControllerApi.class);
            api.addTextMaterial(goalId, request).enqueue(new Callback<GoalMaterialResponse>() {
                @Override
                public void onResponse(@NonNull Call<GoalMaterialResponse> call, @NonNull Response<GoalMaterialResponse> response) {
                    pbLoading.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        if (onComplete != null) onComplete.run();
                        dismiss();
                    } else {
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed: HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GoalMaterialResponse> call, @NonNull Throwable t) {
                    pbLoading.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
