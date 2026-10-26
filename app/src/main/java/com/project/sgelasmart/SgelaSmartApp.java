package com.project.sgelasmart;

import android.app.Application;
import com.project.sgelasmart.core.api.ApiClient;

public class SgelaSmartApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.initialize(this);
    }
}
