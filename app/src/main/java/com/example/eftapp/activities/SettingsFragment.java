package com.example.eftapp.activities;

import android.app.TimePickerDialog;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.eftapp.R;

import java.util.Calendar;
import java.util.Locale;

import util.SettingsManager;

public class SettingsFragment extends Fragment {

    private Spinner spinnerGender, spinnerAge;
    private Button developerButton, buttonFirstNotification, buttonSecondNotification;
    private SettingsManager settingsManager;
    private SharedPreferences notificationPrefs;
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_FIRST_NOTIFICATION_TIME = "first_notification_time";
    private static final String KEY_SECOND_NOTIFICATION_TIME = "second_notification_time";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and return the layout directly
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize SettingsManager
        settingsManager = new SettingsManager(requireContext());

        // Initialize SharedPreferences for notification times
        notificationPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Set up the Toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            // Use ContextCompat to get the color
            toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            toolbar.setTitle("Voice Settings");
        }

        // Initialize Spinners
        spinnerGender = view.findViewById(R.id.spinnerGender);
        spinnerAge = view.findViewById(R.id.spinnerAge);

        // Set up adapters for Spinners
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

        // Load saved settings
        loadSavedSettings();

        // Set up Spinner listeners
        setupSpinnerListener(spinnerGender);
        setupSpinnerListener(spinnerAge);

        // Initialize Notification Time Buttons
        buttonFirstNotification = view.findViewById(R.id.buttonFirstNotification);
        buttonSecondNotification = view.findViewById(R.id.buttonSecondNotification);

        // Load saved notification times
        loadNotificationTimes();

        // Set up click listeners for the notification time buttons
        buttonFirstNotification.setOnClickListener(v -> showTimePickerDialog(KEY_FIRST_NOTIFICATION_TIME));
        buttonSecondNotification.setOnClickListener(v -> showTimePickerDialog(KEY_SECOND_NOTIFICATION_TIME));

        // Developer Button
        developerButton = view.findViewById(R.id.developerButton);
        developerButton.setOnClickListener(view1 -> {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Remove specific key-value pair
            editor.apply();
        });

        return view;
    }

    private void setupSpinnerListener(Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveSettings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });
    }

    private void loadSavedSettings() {
        // Load saved settings and set Spinner selections
        String savedGender = settingsManager.getGender();
        String savedAge = settingsManager.getAge();

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
        String age = spinnerAge.getSelectedItem().toString();

        // Map selected options to the corresponding value
        String selectedValue = mapSelectionToValue(gender, age);
        Log.d("SettingsFragment", "Selected Value: " + selectedValue);

        // Save settings
        settingsManager.saveSettings(gender, age, selectedValue);
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
            return "default_value"; // Fallback value
        }
    }

    private void loadNotificationTimes() {
        // Load saved notification times
        String firstTime = notificationPrefs.getString(KEY_FIRST_NOTIFICATION_TIME, "08:00");
        String secondTime = notificationPrefs.getString(KEY_SECOND_NOTIFICATION_TIME, "20:00");

        // Set the button text to the saved times
        buttonFirstNotification.setText(firstTime);
        buttonSecondNotification.setText(secondTime);
    }

    private void showTimePickerDialog(String key) {
        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    // Format the selected time
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                    // Save the selected time
                    saveNotificationTime(key, time);

                    // Update the button text
                    if (key.equals(KEY_FIRST_NOTIFICATION_TIME)) {
                        buttonFirstNotification.setText(time);
                    } else {
                        buttonSecondNotification.setText(time);
                    }

                    // Notify the user
                    Toast.makeText(requireContext(), "Notification time set to " + time, Toast.LENGTH_SHORT).show();
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY), // Default hour
                Calendar.getInstance().get(Calendar.MINUTE), // Default minute
                true // 24-hour format
        );

        // Show the dialog
        timePickerDialog.show();
    }

    private void saveNotificationTime(String key, String time) {
        // Save the notification time in SharedPreferences
        SharedPreferences.Editor editor = notificationPrefs.edit();
        editor.putString(key, time);
        editor.apply();
    }
}