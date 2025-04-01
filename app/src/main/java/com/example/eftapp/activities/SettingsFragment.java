package com.example.eftapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.eftapp.R;
import com.example.eftapp.databinding.FragmentSettingsBinding;

import util.SettingsManager;

public class SettingsFragment extends Fragment {

    private Spinner spinnerGender, spinnerAge; // Removed spinnerLanguage
    private Button developerButton;
    private SettingsManager settingsManager;
    private FragmentSettingsBinding binding;

    private int secretTapCount = 0;
    private static final int SECRET_TAP_THRESHOLD = 7;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Use ViewBinding to inflate the layout
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize SettingsManager
        settingsManager = new SettingsManager(requireContext());

        // Set up the Toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            toolbar.setTitle("Settings");
        }

        // Initialize Spinners
        spinnerGender = binding.spinnerGender;
        spinnerAge = binding.spinnerAge;

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.age_options,
                android.R.layout.simple_spinner_item
        );
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(ageAdapter);

        loadSavedSettings();
        setupSpinnerListener(spinnerGender);
        setupSpinnerListener(spinnerAge);

        developerButton = binding.developerButton;
        developerButton.setVisibility(View.GONE); // Initially hide

        developerButton.setOnClickListener(view1 -> {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        });

        // SECRET TAP DETECTOR (For example, on the toolbar title)
        toolbar.setOnClickListener(v -> {
            secretTapCount++;
            if (secretTapCount >= SECRET_TAP_THRESHOLD) {
                toggleDeveloperButtonVisibility();
                secretTapCount = 0; // Reset
            }
        });

        // Load saved times from SharedPreferences to display on buttons
        SharedPreferences preferences = requireActivity().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        int firstHour = preferences.getInt("first_notification_hour", 9);
        int firstMinute = preferences.getInt("first_notification_minute", 0);
        int secondHour = preferences.getInt("second_notification_hour", 18);
        int secondMinute = preferences.getInt("second_notification_minute", 0);

        binding.buttonFirstNotification.setText(String.format("%02d:%02d", firstHour, firstMinute));
        binding.buttonSecondNotification.setText(String.format("%02d:%02d", secondHour, secondMinute));

        binding.buttonFirstNotification.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).showTimePickerDialog(1001);
        });

        binding.buttonSecondNotification.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).showTimePickerDialog(1002);
        });

        return view;
    }

    private void toggleDeveloperButtonVisibility() {
        if (developerButton.getVisibility() == View.VISIBLE) {
            developerButton.setVisibility(View.GONE);
        } else {
            developerButton.setVisibility(View.VISIBLE);
        }
        developerButton.postInvalidate(); // Immediate UI update
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupSpinnerListener(Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveSettings();
                spinner.postInvalidate(); // Immediate UI update
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });
    }

    private void loadSavedSettings() {
        String savedGender = settingsManager.getGender();
        String savedAge = settingsManager.getAge();
        String savedValue = settingsManager.getValue();
        Log.d("SettingsFragment", "Loading - Gender: " + savedGender + ", Age: " + savedAge + ", Value: " + savedValue);
        setSpinnerSelection(spinnerGender, savedGender);
        setSpinnerSelection(spinnerAge, savedAge);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private void saveSettings() {
        // Get selected values
        String gender = spinnerGender.getSelectedItem().toString();
        String age = spinnerAge.getSelectedItem().toString(); // Removed language

        // Map selected options to the corresponding value
        String selectedValue = mapSelectionToValue(gender, age); // Removed language
        Log.d("SettingsFragment", "Selected Value: " + selectedValue);

        // Save settings
        settingsManager.saveSettings(gender, age, selectedValue); // Removed language
    }

    private String mapSelectionToValue(String gender, String age) {
        // Map the selected options to the corresponding value
        if (gender.equals("Female") && age.equals("Young")) {
            return "63b407aa241a82001d51b97d";
        } else if (gender.equals("Male") && age.equals("Young")) {
            return "63b407db241a82001d51b9f5";
        } else if (gender.equals("Male") && age.equals("Mature")) {
            return "6380894dd72424f0cfbdbe97";
        } else if (gender.equals("Female") && age.equals("Mature")) {
            return "64703b9541838e0023bcef48";
        } else {
            return "6380894dd72424f0cfbdbe97"; // Fallback value
        }
    }
}