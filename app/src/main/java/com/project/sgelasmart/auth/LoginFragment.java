package com.project.sgelasmart.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.project.sgelasmart.MainActivity;
import com.project.sgelasmart.R;
import com.project.sgelasmart.api.model.LoginRequest;
import com.project.sgelasmart.api.model.LoginResponse;
import com.project.sgelasmart.api.service.AuthControllerApi;
import com.project.sgelasmart.core.api.ApiClient;
import com.project.sgelasmart.core.auth.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar pbLoading;
    private TextView tvStatus, tvGoRegister;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        pbLoading = view.findViewById(R.id.pb_loading);
        tvStatus = view.findViewById(R.id.tv_login_status);
        tvGoRegister = view.findViewById(R.id.tv_go_register);

        if (getArguments() != null) {
            String prefilledEmail = getArguments().getString("prefilled_email");
            String statusMessage = getArguments().getString("status_message");

            if (prefilledEmail != null) {
                etEmail.setText(prefilledEmail);
                etPassword.requestFocus();
            }
            if (statusMessage != null) {
                tvStatus.setText(statusMessage);
                tvStatus.setVisibility(View.VISIBLE);
            }
        }

        btnLogin.setOnClickListener(v -> performLogin());

        tvGoRegister.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showRegister();
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        setLoading(true);
        tvStatus.setVisibility(View.GONE);

        AuthControllerApi authApi = ApiClient.createService(AuthControllerApi.class);

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse data = response.body();
                    sessionManager.saveSession(data.getToken(), data.getId(), data.getEmail(), data.getName());

                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    tvStatus.setText("Invalid email or password");
                    tvStatus.setTextColor(0xFFFF6B6B);
                    tvStatus.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                tvStatus.setText("Network error: Could not connect to server");
                tvStatus.setTextColor(0xFFFF6B6B);
                tvStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
}
