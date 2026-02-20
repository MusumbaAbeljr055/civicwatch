package com.mihneacristian.civicwatch.presentation.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mihneacristian.civicwatch.R;
import com.mihneacristian.civicwatch.databinding.FragmentAboutUsBinding;

public class AboutUsFragment extends Fragment {

    private FragmentAboutUsBinding binding;
    private DatabaseReference databaseReference;
    private static final String TAG = "AboutUsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAboutUsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        setupAboutUsContent();
        setupClickListeners();
        loadStats();
    }

    private void setupAboutUsContent() {
        // Get version
        String versionName = getVersionName();
        binding.versionText.setText(versionName);

        // Set app name
        binding.appName.setText(getString(R.string.app_name));

        // Set about us text
        binding.aboutUsText.setText(getString(R.string.about_us_text));

        // Set author name
        binding.authorNameValue.setText(getString(R.string.author_name));

        // Set email
        binding.authorEmail.setText(getString(R.string.author_email));

        // Set location
        binding.city.setText(getString(R.string.city));
    }

    private void loadStats() {
        // Load total issues count
        databaseReference.child("stats").child("totalIssues")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long totalCount = dataSnapshot.getValue(Long.class);
                        if (totalCount != null) {
                            // You can update the stats in the layout if you want dynamic numbers
                            // For now, we're using static text
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private String getVersionName() {
        try {
            PackageInfo packageInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }

    private void setupClickListeners() {
        // Email click
        binding.authorEmail.setOnClickListener(v -> sendEmail());

        // Copy email button
        binding.btnCopyEmail.setOnClickListener(v -> copyEmailToClipboard());

        // Contact info click
        binding.contactInfo1.setOnClickListener(v -> sendEmail());

        // Social media buttons with your actual handles
        binding.btnTwitter.setOnClickListener(v -> openTwitter());
        binding.btnFacebook.setOnClickListener(v -> openFacebook());
        binding.btnGithub.setOnClickListener(v -> openGithub());

        // WhatsApp button - Add this if you want to add WhatsApp icon
        // For now, let's add a long press on email to open WhatsApp
        binding.authorEmail.setOnLongClickListener(v -> {
            openWhatsApp();
            return true;
        });

        // Rate us button
        binding.btnRateUs.setOnClickListener(v -> rateUsOnPlayStore());
    }

    private void openTwitter() {
        try {
            // Try to open Twitter app first
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=AbelJr150"));
            startActivity(intent);
        } catch (Exception e) {
            // If Twitter app is not installed, open in browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/AbelJr150"));
            startActivity(intent);
        }
    }

    private void openFacebook() {
        try {
            // Try to open Facebook app first
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/100093542320811")); // You'll need your Facebook ID
            startActivity(intent);
        } catch (Exception e) {
            // If Facebook app is not installed, open in browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/abeljr150"));
            startActivity(intent);
        }
    }

    private void openGithub() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MusumbaAbeljr055"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot open GitHub", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp() {
        try {
            String url = "https://wa.me/256705149399?text=Hello%20CivicWatch%20Team";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.author_email)});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about " + getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, buildEmailBody());

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } else {
                Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void copyEmailToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Email", getString(R.string.author_email));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Email copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void rateUsOnPlayStore() {
        // Replace with your Play Store link when published
        String playStoreLink = "https://play.google.com/store/apps/details?id=" +
                requireContext().getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreLink));
        startActivity(intent);
    }

    private String buildEmailBody() {
        return "Feedback about " + getString(R.string.app_name) + "\n\n" +
                "App Version: " + getVersionName() + "\n" +
                "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + "\n" +
                "Android Version: " + android.os.Build.VERSION.RELEASE + "\n\n" +
                "Please write your message here:";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}