package com.mihneacristian.civicwatch.presentation.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.FragmentSettingsBinding;
import com.mihneacristian.civicwatch.presentation.activities.LanguageSettingsActivity;
import com.mihneacristian.civicwatch.utils.LocaleHelper;
import com.mihneacristian.civicwatch.utils.ThemeManager;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private ThemeManager themeManager;
    private LocaleHelper localeHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themeManager = new ThemeManager(requireContext());
        localeHelper = new LocaleHelper(requireContext());

        setupThemeRadioGroup();
        updateLanguageDisplay();
        setupClickListeners();
    }

    private void setupThemeRadioGroup() {
        int currentTheme = themeManager.getThemeMode();

        switch (currentTheme) {
            case ThemeManager.THEME_LIGHT:
                binding.radioLight.setChecked(true);
                break;
            case ThemeManager.THEME_DARK:
                binding.radioDark.setChecked(true);
                break;
            case ThemeManager.THEME_SYSTEM:
            default:
                binding.radioSystem.setChecked(true);
                break;
        }

        binding.radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_light) {
                themeManager.setThemeMode(ThemeManager.THEME_LIGHT);
                Toast.makeText(getContext(), R.string.light_mode_applied, Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
            } else if (checkedId == R.id.radio_dark) {
                themeManager.setThemeMode(ThemeManager.THEME_DARK);
                Toast.makeText(getContext(), R.string.dark_mode_applied, Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
            } else if (checkedId == R.id.radio_system) {
                themeManager.setThemeMode(ThemeManager.THEME_SYSTEM);
                Toast.makeText(getContext(), R.string.system_default_applied, Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
            }
        });
    }

    private void updateLanguageDisplay() {
        String currentLanguage = localeHelper.getLanguage();
        String languageDisplay = getLanguageDisplayName(currentLanguage);
        binding.tvSelectedLanguage.setText(languageDisplay);
    }

    private String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case "es":
                return "Español";
            case "fr":
                return "Français";
            case "lg":
                return "Luganda";
            case "en":
            default:
                return "English";
        }
    }

    private void setupClickListeners() {
        binding.btnNotifications.setOnClickListener(v -> {
            Toast.makeText(getContext(), R.string.notifications_settings, Toast.LENGTH_SHORT).show();
        });

        binding.btnLanguage.setOnClickListener(v -> {
            // Open Language Settings Activity
            Intent intent = new Intent(getContext(), LanguageSettingsActivity.class);
            startActivity(intent);
        });

        binding.btnAbout.setOnClickListener(v -> {
            // Navigate to About Us fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, new AboutUsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnPrivacy.setOnClickListener(v -> {
            Toast.makeText(getContext(), R.string.privacy_policy, Toast.LENGTH_SHORT).show();
        });

        binding.btnTerms.setOnClickListener(v -> {
            Toast.makeText(getContext(), R.string.terms_of_service, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update language display when returning to fragment
        updateLanguageDisplay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}