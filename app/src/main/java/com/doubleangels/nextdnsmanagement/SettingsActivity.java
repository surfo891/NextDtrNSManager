package com.doubleangels.nextdnsmanagement;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.doubleangels.nextdnsmanagement.sentry.SentryInitializer;
import com.doubleangels.nextdnsmanagement.sentry.SentryManager;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    public SentryManager sentryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sentryManager = new SentryManager(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            if (sentryManager.isEnabled()) {
                SentryInitializer.initialize(this);
            }
            String appLocale = setupLanguageForActivity();
            sentryManager.captureMessage("Using locale: " + appLocale);
            setupDarkModeForActivity(sharedPreferences);
            initializeViews();
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
        new ContextThemeWrapper(getBaseContext(), R.style.AppTheme).applyOverrideConfiguration(newConfig);
        return appLocale.getLanguage();
    }

    private void setupDarkModeForActivity(SharedPreferences sharedPreferences) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            String darkMode = sharedPreferences.getString("dark_mode", "match");
            sentryManager.captureMessage("Dark mode setting: " + darkMode);
            if (darkMode.contains("match")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if (darkMode.contains("on")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    private void initializeViews() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commitNow();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            setInitialSentryVisibility(sharedPreferences);
            SwitchPreference sentryEnablePreference = findPreference("sentry_enable");
            ListPreference darkModePreference = findPreference("dark_mode");
            if (sentryEnablePreference != null) {
                setupSentryChangeListener(sentryEnablePreference, sharedPreferences);
            }
            if (darkModePreference != null) {
            setupDarkModeChangeListener(darkModePreference, sharedPreferences);
        }

            setupButton("whitelist_domain_1_button", R.string.whitelist_domain_1);
            setupButton("whitelist_domain_2_button", R.string.whitelist_domain_2);
            setupButton("sentry_info_button", R.string.sentry_info_url);
            setupButtonForIntent("author_button");
            setupButton("feedback_button", R.string.feedback_url);
            setupButton("github_button", R.string.github_url);
            setupButton("github_issue_button", R.string.github_issues_url);
            setupButton("donation_button", R.string.donation_url);
            setupButton("translate_button", R.string.translate_url);
            setupButton("privacy_policy_button", R.string.privacy_policy_url);
            setupButton("nextdns_privacy_policy_button", R.string.nextdns_privacy_policy_url);
            setupButton("nextdns_user_agreement_button", R.string.nextdns_user_agreement_url);
            setupButtonForIntent("permission_button");
            setupButton("version_button", R.string.versions_url);
            String versionName = BuildConfig.VERSION_NAME;
            Preference versionPreference = findPreference("version_button");
            if (versionPreference != null) {
                versionPreference.setSummary(versionName);
            }
        }

        private void setInitialSentryVisibility(SharedPreferences sharedPreferences) {
            boolean visibility = sharedPreferences.getBoolean("sentry_enable", false);
            setPreferenceVisibility("whitelist_domains", visibility);
            setPreferenceVisibility("whitelist_domain_1_button", visibility);
            setPreferenceVisibility("whitelist_domain_2_button", visibility);
        }

        private void setupButton(String buttonKey, int textResource) {
            Preference button = findPreference(buttonKey);
            assert button != null;
            button.setOnPreferenceClickListener(preference -> {
                if ("whitelist_domain_1_button".equals(buttonKey) || "whitelist_domain_2_button".equals(buttonKey)) {
                    ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    CharSequence copiedText = getString(textResource);
                    ClipData copiedData = ClipData.newPlainText("text", copiedText);
                    clipboardManager.setPrimaryClip(copiedData);
                    Toast.makeText(getContext(), "Text copied!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(textResource)));
                    startActivity(intent);
                }
                return true;
            });
        }

        private void setupButtonForIntent(String buttonKey) {
            Preference button = findPreference(buttonKey);
            assert button != null;
            button.setOnPreferenceClickListener(preference -> {
                if ("author_button".equals(buttonKey)) {
                    Intent intent = new Intent(getContext(), AuthorActivity.class);
                    startActivity(intent);
                }
                if ("permission_button".equals(buttonKey)) {
                    Intent intent = new Intent(getContext(), PermissionActivity.class);
                    startActivity(intent);
                }
                return true;
            });
        }

        private void setupDarkModeChangeListener(ListPreference setting, SharedPreferences sharedPreferences) {
            setting.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.i("Output","Output: " + newValue.toString());
                sharedPreferences.edit().putString("dark_mode", newValue.toString()).apply();
                ProcessPhoenix.triggerRebirth(requireContext());
                return true;
            });
        }

        private void setupSentryChangeListener(SwitchPreference switchPreference, SharedPreferences sharedPreferences) {
            if (switchPreference != null) {
                switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEnabled = (boolean) newValue;
                    SharedPreferences.Editor preferenceEdit = sharedPreferences.edit();
                    preferenceEdit.putBoolean("sentry_enable", isEnabled);
                    setPreferenceVisibility("whitelist_domains", isEnabled);
                    setPreferenceVisibility("whitelist_domain_1_button", isEnabled);
                    setPreferenceVisibility("whitelist_domain_2_button", isEnabled);
                    preferenceEdit.apply();
                    return true;
                });
            }
        }

        private void setPreferenceVisibility(String key, Boolean visibility) {
            Preference preference = findPreference(key);
            if (preference != null) {
                preference.setVisible(visibility);
            }
        }
    }
}
