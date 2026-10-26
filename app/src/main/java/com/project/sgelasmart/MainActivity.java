package com.project.sgelasmart;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.project.sgelasmart.auth.AuthActivity;
import com.project.sgelasmart.core.auth.SessionManager;
import com.project.sgelasmart.task.index.TaskIndexFragment;
import com.project.sgelasmart.core.ui.PlaceholderFragment;
import com.project.sgelasmart.goal.index.GoalListFragment;

public class MainActivity extends AppCompatActivity {

    private ImageView navHome, navGoals, navTasks, navCalendar, navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        navHome = findViewById(R.id.nav_home);
        navGoals = findViewById(R.id.nav_goals);
        navTasks = findViewById(R.id.nav_tasks);
        navCalendar = findViewById(R.id.nav_calendar);
        navSettings = findViewById(R.id.nav_settings);

        navHome.setOnClickListener(v -> selectTab(navHome, PlaceholderFragment.newInstance("Dashboard", "Overview & daily progress")));
        navGoals.setOnClickListener(v -> selectTab(navGoals, new GoalListFragment()));
        navTasks.setOnClickListener(v -> selectTab(navTasks, new TaskIndexFragment()));
        navCalendar.setOnClickListener(v -> selectTab(navCalendar, PlaceholderFragment.newInstance("Calendar", "View your schedule and deadlines")));
        navSettings.setOnClickListener(v -> selectTab(navSettings, PlaceholderFragment.newInstance("Settings", "Configure your account and preferences")));

        if (savedInstanceState == null) {
            selectTab(navGoals, new GoalListFragment());
        }
    }

    private void selectTab(ImageView activeNav, Fragment fragment) {
        // Reset all icons
        resetNav(navHome);
        resetNav(navGoals);
        resetNav(navTasks);
        resetNav(navCalendar);
        resetNav(navSettings);

        // Highlight active icon
        activeNav.setBackgroundResource(R.drawable.bg_active_tab);
        activeNav.setColorFilter(Color.parseColor("#FFFFFF"));

        // Load Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_container, fragment)
                .commit();
    }

    private void resetNav(ImageView nav) {
        nav.setBackground(null);
        nav.setColorFilter(Color.parseColor("#6E7681"));
    }
}
