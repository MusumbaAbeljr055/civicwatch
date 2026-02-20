package com.mihneacristian.civicwatch.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "selected_language";
    private static SharedPreferences sharedPreferences;

    public LocaleHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean setLocale(String languageCode) {
        if (!languageCode.equals(getLanguage())) {
            sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply();
            return true;
        }
        return false;
    }

    public String getLanguage() {
        return sharedPreferences != null ?
                sharedPreferences.getString(KEY_LANGUAGE, "en") : "en";
    }

    public static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, displayMetrics);
            return context;
        }
    }

    public static void applyLanguage(Context context) {
        String languageCode = "en";
        if (sharedPreferences != null) {
            languageCode = sharedPreferences.getString(KEY_LANGUAGE, "en");
        } else {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            languageCode = sharedPreferences.getString(KEY_LANGUAGE, "en");
        }
        updateResources(context, languageCode);
    }
}