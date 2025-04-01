package util;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_AGE = "age"; // Removed KEY_LANGUAGE
    private static final String KEY_VALUE = "value";

    private final SharedPreferences sharedPreferences;

    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Save selected options
    public void saveSettings(String gender, String age, String value) { // Removed language
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_GENDER, gender);
        editor.putString(KEY_AGE, age); // Removed language
        editor.putString(KEY_VALUE, value);
        editor.apply(); // Save changes to SharedPreferences
    }

    // Load saved options
    public String getGender() {
        return sharedPreferences.getString(KEY_GENDER, "Male"); // Default to Male
    }

    public String getAge() {
        return sharedPreferences.getString(KEY_AGE, "Mature"); // Default to Mature
    }

    public String getValue() {
        return sharedPreferences.getString(KEY_VALUE, "6380894dd72424f0cfbdbe97"); // Default value Male Mature
    }
}