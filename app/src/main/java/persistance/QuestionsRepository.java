package com.example.eftapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import util.QuestionsApi;
import util.UserManager;

public class QuestionsRepository {
    public static final String PREFS_NAME = "MyPrefs";
    private Context context;
    private String goal;
    private int userId;

    public QuestionsRepository(Context context) {
        this.context = context;
        UserManager userManager = new UserManager(context);
        this.userId = userManager.getUserId();
    }

    // Fetch the questions using the API
    public void getAiQuestions(String goalInput, QuestionsApi.ApiResponseCallback callback) {
        QuestionsApi questionsApi = new QuestionsApi(context, callback);
        goal = goalInput;
        questionsApi.getAiQuestions(goalInput, userId);
    }

    // Save the questions string into SharedPreferences
    public void saveQuestionsToSharedPreferences(String questions) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("questions", questions);
        editor.putString("long_term_goal", goal);
        editor.apply();
    }

    // Retrieve the stored questions string
    public String getQuestionsFromSharedPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString("questions", null);
    }
}
