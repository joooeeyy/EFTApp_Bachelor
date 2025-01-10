package com.example.eftapp.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.eftapp.R;

import ViewModel.ApiViewModel;
import ViewModel.QuestionsViewModel;


public class GoalActivity extends AppCompatActivity {

    private EditText longTermGoalInput;
    private Button submitButton;
    private QuestionsViewModel questionsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        // Initialize UI Elements
        TextView instructionText = findViewById(R.id.instruction_text);
        longTermGoalInput = findViewById(R.id.long_term_goal_input);
        submitButton = findViewById(R.id.submit_button);

        questionsViewModel = new ViewModelProvider(this).get(QuestionsViewModel.class);

        // Observe LiveData
        questionsViewModel.fetchQuestionsLiveData().observe(this, success -> {
            if (success != null) {
                if (success) {
                    showLoading(false);
                    finish();
                } else {

                }
            }
        });

        // Handle Submit Button Click
        submitButton.setOnClickListener(v -> {
            String goal = longTermGoalInput.getText().toString().trim();

            if (goal.isEmpty()) {
                Toast.makeText(this, "Please enter a goal before submitting.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show Confirmation Dialog
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Submission")
                    .setMessage("Are you sure you want to submit this as your long-term goal? It cannot be changed later.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        showLoading(true);
                        questionsViewModel.fetchQuestions(goal); // Trigger LiveData update
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });



    }

    private void showLoading(boolean isLoading) {
        RelativeLayout loadingOverlay = findViewById(R.id.loadingOverlay);
        if (isLoading) {
            loadingOverlay.setVisibility(View.VISIBLE); // Show overlay
            submitButton.setEnabled(false); // Disable the button
        } else {
            loadingOverlay.setVisibility(View.GONE); // Hide overlay
            submitButton.setEnabled(true); // Enable the button
        }
    }

//    private void saveLongTermGoal(String goal) {
//        // Save the goal in SharedPreferences
//        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("long_term_goal", goal);
//        editor.apply();
//
//        // Disable input and button
//        longTermGoalInput.setEnabled(false);
//        submitButton.setEnabled(false);
//
//        Toast.makeText(this, "Long-term goal submitted successfully!", Toast.LENGTH_LONG).show();
//    }
}