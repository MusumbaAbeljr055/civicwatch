package com.mihneacristian.civicwatch.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.ActivityLanguageSettingsBinding;
import com.mihneacristian.civicwatch.utils.LocaleHelper;

import java.util.Locale;

public class LanguageSettingsActivity extends AppCompatActivity {

    private ActivityLanguageSettingsBinding binding;
    private LocaleHelper localeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply current language before setting content view
        LocaleHelper.applyLanguage(this);

        binding = ActivityLanguageSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        localeHelper = new LocaleHelper(this);

        setupToolbar();
        setupLanguageRadioGroup();
        setupApplyButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.language_settings);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupLanguageRadioGroup() {
        String currentLanguage = localeHelper.getLanguage();

        switch (currentLanguage) {
            case "en":
                binding.radioEnglish.setChecked(true);
                break;
            case "es":
                binding.radioSpanish.setChecked(true);
                break;
            case "fr":
                binding.radioFrench.setChecked(true);
                break;
            case "lg":
                binding.radioLuganda.setChecked(true);
                break;
            default:
                binding.radioEnglish.setChecked(true);
                break;
        }
    }

    private void setupApplyButton() {
        binding.btnApply.setOnClickListener(v -> {
            int selectedId = binding.radioGroupLanguage.getCheckedRadioButtonId();
            RadioButton selectedRadio = findViewById(selectedId);
            String languageCode = getLanguageCode(selectedRadio.getText().toString());

            if (localeHelper.setLocale(languageCode)) {
                Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show();
                recreateActivity();
            }
        });
    }

    private String getLanguageCode(String languageName) {
        if (languageName.contains("English")) return "en";
        if (languageName.contains("Spanish") || languageName.contains("Español")) return "es";
        if (languageName.contains("French") || languageName.contains("Français")) return "fr";
        if (languageName.contains("Luganda")) return "lg";
        return "en";
    }

    private void recreateActivity() {
        // Restart the main activity to apply language changes
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}