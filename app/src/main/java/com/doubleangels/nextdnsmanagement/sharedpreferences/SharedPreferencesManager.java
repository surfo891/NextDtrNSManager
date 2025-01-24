package com.doubleangels.nextdnsmanagement.sharedpreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "MyAppPreferences";
    private static SharedPreferences sharedPreferences;

    private SharedPreferencesManager() {
        throw new UnsupportedOperationException("Cannot instantiate SharedPreferencesManager.");
    }

    public static synchronized void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    @SuppressLint("ApplySharedPref")
    public static void putString(String key, String value) {
        checkInitialization();
        sharedPreferences.edit().putString(key, value).commit();
    }

    public static String getString(String key, String defaultValue) {
        checkInitialization();
        return sharedPreferences.getString(key, defaultValue);
    }

    @SuppressLint("ApplySharedPref")
    public static void putBoolean(String key, boolean value) {
        checkInitialization();
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        checkInitialization();
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    private static void checkInitialization() {
        if (sharedPreferences == null) {
            throw new IllegalStateException("SharedPreferencesManager is not initialized. Call init() before using it.");
        }
    }
}
