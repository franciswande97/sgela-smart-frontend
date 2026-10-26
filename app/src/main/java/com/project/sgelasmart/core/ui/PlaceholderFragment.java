package com.project.sgelasmart.core.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.sgelasmart.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_BADGE = "arg_badge";

    public static PlaceholderFragment newInstance(String title, String badge) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_BADGE, badge);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvPlaceholderTitle);
        TextView tvBadge = view.findViewById(R.id.tvPlaceholderBadge);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE, "Screen"));
            tvBadge.setText(getArguments().getString(ARG_BADGE, "MODULE"));
        }
    }
}
