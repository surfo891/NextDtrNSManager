package com.doubleangels.nextdnsmanagement;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.doubleangels.nextdnsmanagement.sentry.SentryInitializer;
import com.doubleangels.nextdnsmanagement.sentry.SentryManager;

import java.util.Locale;

public class AuthorActivity extends AppCompatActivity {

    public SentryManager sentryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        sentryManager = new SentryManager(this);
        try {
            if (sentryManager.isEnabled()) {
                SentryInitializer.initialize(this);
            }
            String appLocale = setupLanguageForActivity();
            sentryManager.captureMessage("Using locale: " + appLocale);
            setupPersonalLinks(sentryManager);
        } catch (Exception e) {
            sentryManager.captureException(e);
        }
    }

    private String setupLanguageForActivity() {
        Configuration config = getResources().getConfiguration();
        Locale appLocale = config.getLocales().get(0);
        Locale.setDefault(appLocale);
        Configuration newConfig = new Configuration(config);
        newConfig.setLocale(appLocale);
        return appLocale.getLanguage();
    }

    public void setupPersonalLinks(SentryManager sentryManager) {
        try {
            ImageView githubButton = findViewById(R.id.githubImageView);
            ImageView emailButton = findViewById(R.id.emailImageView);
            ImageView websiteButton = findViewById(R.id.websiteImageView);
            githubButton.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_profile_url)));
                startActivity(intent);
            });
            emailButton.setOnClickListener(view -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:nextdns@doubleangels.com"));
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            });
            websiteButton.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.author_url)));
                startActivity(intent);
            });
        } catch (Exception e) {
            sentryManager.captureException(e);
        }
    }
}
