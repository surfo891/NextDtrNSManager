package com.doubleangels.nextdnsmanagement;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;

import com.doubleangels.nextdnsmanagement.sentry.SentryInitializer;
import com.doubleangels.nextdnsmanagement.sentry.SentryManager;

import java.util.Locale;

public class StatusActivity extends AppCompatActivity {

    public SentryManager sentryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        sentryManager = new SentryManager(this);
        try {
            if (sentryManager.isEnabled()) {
                SentryInitializer.initialize(this);
            }
            setupStatusBarForActivity();
            String appLocale = setupLanguageForActivity();
            sentryManager.captureMessage("Using locale: " + appLocale);
        } catch (Exception e) {
            sentryManager.captureException(e);
        }
    }

    private void setupStatusBarForActivity() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                boolean isLightTheme = (getResources().getConfiguration().uiMode &
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                        android.content.res.Configuration.UI_MODE_NIGHT_NO;
                insetsController.setSystemBarsAppearance(
                        isLightTheme ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }
    }

    private String setupLanguageForActivity() {
        Configuration config = getResources().getConfiguration();
        Locale appLocale = config.getLocales().get(0);
        Locale.setDefault(appLocale);
        Configuration newConfig = new Configuration(config);
        newConfig.setLocale(appLocale);
        new ContextThemeWrapper(getBaseContext(), R.style.AppTheme).applyOverrideConfiguration(newConfig);
        return appLocale.getLanguage();
    }
}
