package com.project.sgelasmart.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.project.sgelasmart.R;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            showLogin(null, null);
        }
    }

    public void showLogin(String prefilledEmail, String statusMessage) {
        LoginFragment fragment = new LoginFragment();
        if (prefilledEmail != null || statusMessage != null) {
            Bundle bundle = new Bundle();
            bundle.putString("prefilled_email", prefilledEmail);
            bundle.putString("status_message", statusMessage);
            fragment.setArguments(bundle);
        }
        replaceFragment(fragment);
    }

    public void showRegister() {
        replaceFragment(new RegisterFragment());
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, fragment)
                .commit();
    }
}
