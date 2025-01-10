package util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String USER_ID_KEY = "user_id";
    private final SharedPreferences preferences;

    public UserManager(Context context) {
        preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    // Get the user ID (as an int)
    public int getUserId() {
        return preferences.getInt(USER_ID_KEY, -1); // -1 is the default if no user ID exists
    }

    // Generate and save a user ID
    public void generateAndSaveUserId() {
        int newUserId = generateUniqueId();
        preferences.edit().putInt(USER_ID_KEY, newUserId).apply(); // Save as int
    }

    // Generate unique ID
    private int generateUniqueId() {
        long time = System.currentTimeMillis(); // Get current time in milliseconds
        int random = (int) (Math.random() * 1000); // Random number between 0 and 999
        return (int) ((time % 100000) + random); // Combine time and random, and cast to int
    }
}
