package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public class PollManager {
    private String[] questions;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String FIRST_POLL_DATE_KEY = "first_poll_date";
    private static final String LAST_RESET_TIME_KEY = "last_reset_time"; // New key for tracking last reset time

    public static final long ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000; // 1 week in milliseconds
    public static final int FUTURE_EVENTS_PER_DAY = 2;

    private static final long TEN_MINUTES_MILLIS = 10 * 60 * 1000; // 10 minutes in milliseconds

    public PollManager(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedGoal = preferences.getString("questions", null);

        Log.d("questions", savedGoal);

        if (savedGoal != null && !savedGoal.isEmpty()) {
            questions = formatString(savedGoal);
        } else {
            // Handle the case where the saved goal is null or empty
            questions = new String[0]; // Or you can initialize it with default questions
        }
    }

    public Poll createPoll(String pollType) {
        switch (pollType) {
            case "TSRQ":
                return new TSRQPoll(questions); // Pass the questions to TSRQPoll constructor
            case "AdjustingAmount":
                return new AdjustingAmountPoll(); // If AdjustingAmountPoll needs questions
            case "Belohnungsaufschub":
                return new BelohnungsaufschubPoll(); // If BelohnungsaufschubPoll needs questions
            default:
                throw new IllegalArgumentException("Unknown poll type");
        }
    }

    public static void saveFirstPollDate(Context context) {
        long currentTime = System.currentTimeMillis(); // Get the current time in milliseconds
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(FIRST_POLL_DATE_KEY, currentTime);
        editor.apply();
    }

    public static long getFirstPollDate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(FIRST_POLL_DATE_KEY, -1); // Return -1 if not found
    }

    public static boolean shouldShowPollAgain(Context context) {
        long firstPollDate = getFirstPollDate(context);
        if (firstPollDate == -1) {
            return true; // Poll hasn't been taken yet
        }

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - firstPollDate;

        // If more than a week has passed, show the poll again
        return timeDifference >= ONE_WEEK_MILLIS;
    }

    public static int getFutureEventsLeft(Context context) {
        // Reset future events if needed (e.g., at midnight)
        resetFutureEventsIfNeeded(context);

        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getInt("future_events_left", FUTURE_EVENTS_PER_DAY); // Default to 2
    }

    public static void setFutureEventsLeft(Context context, int futureEventsLeft) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("future_events_left", futureEventsLeft);
        editor.apply();
    }

    /**
     * Resets the future_events_left count to FUTURE_EVENTS_PER_DAY if midnight has passed since the last reset.
     */
    public static void resetFutureEventsIfNeeded(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        long lastResetTime = preferences.getLong(LAST_RESET_TIME_KEY, -1); // Default to -1 if not set
        long currentTime = System.currentTimeMillis();

        // Check if midnight has passed since the last reset
        if (lastResetTime == -1 || isMidnightPassed(lastResetTime, currentTime)) {
            // Reset the future_events_left count to FUTURE_EVENTS_PER_DAY
            setFutureEventsLeft(context, FUTURE_EVENTS_PER_DAY);

            // Update the last reset time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(LAST_RESET_TIME_KEY, currentTime);
            editor.apply();
        }
    }

    /**
     * Checks if midnight has passed between two timestamps.
     *
     * @param lastResetTime The last reset time in milliseconds.
     * @param currentTime   The current time in milliseconds.
     * @return True if midnight has passed, false otherwise.
     */
    private static boolean isMidnightPassed(long lastResetTime, long currentTime) {
        Calendar lastResetCalendar = Calendar.getInstance();
        lastResetCalendar.setTimeInMillis(lastResetTime);

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(currentTime);

        // Check if the current date is after the last reset date (i.e., midnight has passed)
        return currentCalendar.get(Calendar.DAY_OF_YEAR) > lastResetCalendar.get(Calendar.DAY_OF_YEAR) ||
                currentCalendar.get(Calendar.YEAR) > lastResetCalendar.get(Calendar.YEAR);
    }

    private String[] formatString(String longString) {
        // Split the string into an array of strings using one or more newlines
        return longString.split("\n+");
    }
}