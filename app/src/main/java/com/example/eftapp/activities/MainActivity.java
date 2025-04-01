package com.example.eftapp.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.eftapp.R;
import com.example.eftapp.databinding.ActivityMainBinding;

import java.util.Calendar;

import Notification.NotificationReceiver;
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

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int firstHour = preferences.getInt("first_notification_hour", 9);  // Default to 09:00 AM
        int firstMinute = preferences.getInt("first_notification_minute", 0);

        int secondHour = preferences.getInt("second_notification_hour", 18); // Default to 06:00 PM
        int secondMinute = preferences.getInt("second_notification_minute", 0);

        scheduleNotification(firstHour, firstMinute, 1001);
        scheduleNotification(secondHour, secondMinute, 1002);


        createNotificationChannel();
        requestNotificationPermission();

        // Schedule two notifications at different times



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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "my_channel_id",
                    "My Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void showTimePickerDialog(int requestCode) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        new android.app.TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            // Save time to SharedPreferences
            saveNotificationTime(requestCode, selectedHour, selectedMinute);

            // Schedule notification
            scheduleNotification(selectedHour, selectedMinute, requestCode);

            // Update the button text immediately
            if (requestCode == 1001) {
                Button buttonFirstNotification = findViewById(R.id.buttonFirstNotification);
                buttonFirstNotification.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
            } else if (requestCode == 1002) {
                Button buttonSecondNotification = findViewById(R.id.buttonSecondNotification);
                buttonSecondNotification.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
            }

            // Feedback to the user
            Toast.makeText(this, "Notification set for " + String.format("%02d:%02d", selectedHour, selectedMinute), Toast.LENGTH_SHORT).show();
        }, hour, minute, true).show();
    }



    private void saveNotificationTime(int requestCode, int hour, int minute) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (requestCode == 1001) {
            editor.putInt("first_notification_hour", hour);
            editor.putInt("first_notification_minute", minute);
        } else if (requestCode == 1002) {
            editor.putInt("second_notification_hour", hour);
            editor.putInt("second_notification_minute", minute);
        }

        editor.apply();
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "my_channel_id",
                    "My Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void scheduleNotification(int hour, int minute, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);

        // Each notification needs a unique request code
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If the time is in the past, schedule it for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }




    /**
     * Replaces the current fragment with the selected one.
     */
    private void replaceFragment(Fragment fragment, String title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commitNow(); // Use commitNow() for immediate execution
    }


}