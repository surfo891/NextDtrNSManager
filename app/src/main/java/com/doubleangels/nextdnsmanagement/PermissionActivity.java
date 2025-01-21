package com.doubleangels.nextdnsmanagement;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doubleangels.nextdnsmanagement.adaptors.PermissionsAdapter;
import com.doubleangels.nextdnsmanagement.protocol.VisualIndicator;
import com.doubleangels.nextdnsmanagement.sentry.SentryInitializer;
import com.doubleangels.nextdnsmanagement.sentry.SentryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PermissionActivity extends AppCompatActivity {
    
    private static final int REQUEST_POST_NOTIFICATIONS = 100;
    private static final String POST_NOTIFICATIONS = android.Manifest.permission.POST_NOTIFICATIONS;

    // SentryManager instance for error tracking
    public SentryManager sentryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        // Initialize SentryManager for error tracking
        sentryManager = new SentryManager(this);
        try {
            // Check if Sentry is enabled and initialize it
            if (sentryManager.isEnabled()) {
                SentryInitializer.initialize(this);
            }
            // Setup toolbar
            setupToolbarForActivity();
            // Setup language/locale
            String appLocale = setupLanguageForActivity();
            sentryManager.captureMessage("Using locale: " + appLocale);
            // Setup visual indicator
            setupVisualIndicatorForActivity(sentryManager, this);
            
            // Check and request notification permission if needed
            if (needsNotificationPermission()) {
                requestNotificationPermission();
            }
        } catch (Exception e) {
        RecyclerView recyclerView = findViewById(R.id.permissionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Get list of permissions and set up RecyclerView adapter
        List<PermissionInfo> permissions = getPermissionsList(sentryManager);
        PermissionsAdapter adapter = new PermissionsAdapter(permissions);
        recyclerView.setAdapter(adapter);
    }

    private boolean needsNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            refreshPermissionsList();
        }
    }

    // Setup toolbar for the activity
    private void setupToolbarForActivity() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        // Setup click listener for connection status ImageView
        ImageView imageView = findViewById(R.id.connectionStatus);
        imageView.setOnClickListener(v -> startActivity(new Intent(this, StatusActivity.class)));
    }

    // Setup language/locale for the activity
    private String setupLanguageForActivity() {
        Configuration config = getResources().getConfiguration();
        Locale appLocale = config.getLocales().get(0);
        Locale.setDefault(appLocale);
        Configuration newConfig = new Configuration(config);
        newConfig.setLocale(appLocale);
        new ContextThemeWrapper(getBaseContext(), R.style.AppTheme).applyOverrideConfiguration(newConfig);
        return appLocale.getLanguage();
    }

    // Setup visual indicator for the activity
    private void setupVisualIndicatorForActivity(SentryManager sentryManager, LifecycleOwner lifecycleOwner) {
        try {
            new VisualIndicator(this).initialize(this, lifecycleOwner, this);
        } catch (Exception e) {
            // Catch and log exceptions
            sentryManager.captureException(e);
        }
    }

    private void refreshPermissionsList() {
        RecyclerView recyclerView = findViewById(R.id.permissionRecyclerView);
        if (recyclerView != null) {
            List<PermissionInfo> permissions = getPermissionsList(sentryManager);
            recyclerView.setAdapter(new PermissionsAdapter(permissions));
        }
    }

    // Retrieve the list of permissions requested by the app
    private List<PermissionInfo> getPermissionsList(SentryManager sentryManager) {
        List<PermissionInfo> permissions = new ArrayList<>();
        try {
            // Get package info including requested permissions
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    try {
                        // Skip POST_NOTIFICATIONS permission on pre-Android 13 devices
                        if (permission.equals(POST_NOTIFICATIONS) && 
                            android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                            continue;
                        }
                        
                        PermissionInfo permissionInfo = getPackageManager().getPermissionInfo(permission, 0);
                        if (permissionInfo != null) {
                            permissions.add(permissionInfo);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        sentryManager.captureMessage("Unable to get info for permission: " + permission);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (PackageManager.NameNotFoundException e) {
            // Catch and log exceptions
            sentryManager.captureException(e);
        }
        return permissions;
    }

    // Inflate menu for the activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_back_only, menu);
        return true;
    }

    // Handle menu item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            // Navigate back to SettingsActivity
            Intent mainIntent = new Intent(this, SettingsActivity.class);
            startActivity(mainIntent);
        }
        return super.onContextItemSelected(item);
    }
}
