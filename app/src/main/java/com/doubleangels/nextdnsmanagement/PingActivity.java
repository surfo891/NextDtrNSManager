package com.doubleangels.nextdnsmanagement;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.doubleangels.nextdnsmanagement.protocol.VisualIndicator;
import com.doubleangels.nextdnsmanagement.sentry.SentryInitializer;
import com.doubleangels.nextdnsmanagement.sentry.SentryManager;

import java.util.Locale;

public class PingActivity extends AppCompatActivity {

    // SentryManager instance for error tracking
    public SentryManager sentryManager;
    // WebView instance for displaying web content
    public WebView webView;
    public WebView webView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        sentryManager = new SentryManager(this);
        try {
            if (sentryManager.isEnabled()) {
                SentryInitializer.initialize(this);
            }
            String appLocale = setupLanguageForActivity();
            sentryManager.captureMessage("Using locale: " + appLocale);
            setupVisualIndicatorForActivity(sentryManager, this);
            setupWebViewForActivity(getString(R.string.ping_url), getString(R.string.test_url));
        } catch (Exception e) {
            sentryManager.captureException(e);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
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

    private void setupVisualIndicatorForActivity(SentryManager sentryManager, LifecycleOwner lifecycleOwner) {
        try {
            new VisualIndicator(this).initialize(this, lifecycleOwner, this);
        } catch (Exception e) {
            sentryManager.captureException(e);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setupWebViewForActivity(String url1, String url2) {
        webView = findViewById(R.id.webView);
        webView2 = findViewById(R.id.webView2);
        setupWebView(webView);
        setupWebView(webView2);
        webView.loadUrl(url1);
        webView2.loadUrl(url2);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        webView.setWebViewClient(new WebViewClient());
    }
}
