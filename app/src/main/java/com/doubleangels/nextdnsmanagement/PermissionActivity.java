package com.doubleangels.nextdnsmanagement;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doubleangels.nextdnsmanagement.adaptors.PermissionsAdapter;
import com.doubleangels.nextdnsmanagement.sentry.SentryInitializer;
import com.doubleangels.nextdnsmanagement.sentry.SentryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_POST_NOTIFICATIONS = 100;
    private static final String POST_NOTIFICATIONS = android.Manifest.permission.POST_NOTIFICATIONS;

    private SentryManager sentryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        sentryManager = new SentryManager(this);
        try {
            if (sentryManager.isEnabled()) {
                SentryInitializer.initialize(this);
            }
            String appLocale = setupLanguageForActivity();
            sentryManager.captureMessage("Using locale: " + appLocale);

            if (needsNotificationPermission()) {
                requestNotificationPermission();
            }
        } catch (Exception e) {
            sentryManager.captureException(e);
        }

        RecyclerView recyclerView = findViewById(R.id.permissionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<PermissionInfo> permissions = getPermissionsList(sentryManager);
        recyclerView.setAdapter(new PermissionsAdapter(permissions));
    }

    private boolean needsNotificationPermission() {
        return checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermission() {
        requestPermissions(new String[]{POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            refreshPermissionsList();
        }
    }

    private String setupLanguageForActivity() {
        Locale appLocale = Locale.getDefault();
        Locale.setDefault(appLocale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(appLocale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        return appLocale.getLanguage();
    }

    private void refreshPermissionsList() {
        RecyclerView recyclerView = findViewById(R.id.permissionRecyclerView);
        if (recyclerView != null) {
            List<PermissionInfo> permissions = getPermissionsList(sentryManager);
            recyclerView.setAdapter(new PermissionsAdapter(permissions));
        }
    }

    private List<PermissionInfo> getPermissionsList(SentryManager sentryManager) {
        List<PermissionInfo> permissions = new ArrayList<>();
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    PermissionInfo permissionInfo = getPackageManager().getPermissionInfo(permission, 0);
                    permissions.add(permissionInfo);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            sentryManager.captureException(e);
        }
        return permissions;
    }
}
