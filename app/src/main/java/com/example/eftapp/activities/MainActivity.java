package com.example.eftapp.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.eftapp.R;
import com.example.eftapp.databinding.ActivityMainBinding;

import Notification.NotificationScheduler;
import util.UserManager;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1001; // Request code for notification permission
    private static final int EXACT_ALARM_PERMISSION_CODE = 1002; // Request code for exact alarm permission
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_NOTIFICATIONS_SCHEDULED = "notifications_scheduled";

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force light theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        requestNotificationPermission();

        // Initialize binding and set it as the content view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize UserManager and check if the user ID exists, if not generate it
        UserManager userManager = new UserManager(this);

        // Check if the user ID is -1 (meaning it doesn't exist)
        if (userManager.getUserId() == -1) {
            userManager.generateAndSaveUserId();  // Generate and save a new user ID
        }

        // Set the action bar title
        setTitle("Add Cue"); // Initial title

        binding.bottomNavigationView.setSelectedItemId(R.id.add); //

        // Set initial fragment
        replaceFragment(new AddCueFragment(), "Add Cue");
        binding.bottomNavigationView.setBackground(null);

        // Set up BottomNavigationView item selection listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            String title = "";

            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                selectedFragment = new HomeFragment();
                title = "Home";
            } else if (itemId == R.id.add) {
                selectedFragment = new AddCueFragment();
                title = "Add Cue";
            } else if (itemId == R.id.settings) {
                selectedFragment = new SettingsFragment();
                title = "Settings";
            } else {
                return false;
            }

            replaceFragment(selectedFragment, title);
            return true;
        });

        // Check if notifications are already scheduled
        if (!areNotificationsScheduled()) {
            // Request exact alarm permission for Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestExactAlarmPermission();
            } else {
                // If the device is running Android 11 or lower, schedule notifications directly
                NotificationScheduler.scheduleNotifications(this);
                setNotificationsScheduled(true); // Mark notifications as scheduled
            }
        }
    }

    /**
     * Checks if notifications have already been scheduled.
     */
    private boolean areNotificationsScheduled() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIFICATIONS_SCHEDULED, false);
    }

    /**
     * Marks notifications as scheduled in SharedPreferences.
     */
    private void setNotificationsScheduled(boolean scheduled) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_NOTIFICATIONS_SCHEDULED, scheduled);
        editor.apply();
    }

    /**
     * Requests the SCHEDULE_EXACT_ALARM permission for Android 12+.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestExactAlarmPermission() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivityForResult(intent, EXACT_ALARM_PERMISSION_CODE);
        } else {
            // Permission already granted, schedule notifications
            NotificationScheduler.scheduleNotifications(this);
            setNotificationsScheduled(true); // Mark notifications as scheduled
        }
    }

    /**
     * Handles the result of the permission request.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXACT_ALARM_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    // Permission granted, schedule notifications
                    NotificationScheduler.scheduleNotifications(this);
                    setNotificationsScheduled(true); // Mark notifications as scheduled
                } else {
                    // Permission denied, show a message or disable notification functionality
                    Toast.makeText(this, "Exact alarm permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Replaces the current fragment with the selected one.
     */
    private void replaceFragment(Fragment fragment, String title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

        // Set the action bar title
        setTitle(title);
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

}