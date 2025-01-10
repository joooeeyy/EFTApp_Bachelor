package util;

import android.content.Context;
import android.content.SharedPreferences;

public class PollManager {
    private String[] questions;

    public PollManager(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedGoal = preferences.getString("questions", null);

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

    private String[] formatString(String longString) {
        // Split the string into an array of strings using one or more newlines
        return longString.split("\n+");
    }
}
