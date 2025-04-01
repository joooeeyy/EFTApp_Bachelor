package com.example.eftapp.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.eftapp.R;
import ViewModel.QuestionsViewModel;

public class GoalActivity extends AppCompatActivity {

    private EditText longTermGoalInput;
    private EditText ageInput;
    private RadioGroup genderInput;
    private Button submitButton;
    private QuestionsViewModel questionsViewModel;
    private String goal;
    private String age;
    private String gender;

    private static final String DEFAULT_GOAL_PREFIX = "I want to ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        // Initialize UI Elements
        TextView instructionText = findViewById(R.id.instruction_text);
        longTermGoalInput = findViewById(R.id.long_term_goal_input);
        genderInput = findViewById(R.id.gender_radio_group);
        ageInput = findViewById(R.id.age_input);
        submitButton = findViewById(R.id.submit_button);

        questionsViewModel = new ViewModelProvider(this).get(QuestionsViewModel.class);

        // Observe LiveData
        questionsViewModel.fetchQuestionsLiveData().observe(this, success -> {
            if (success != null) {
                if (success) {
                    saveLongTermGoal(goal);
                    showLoading(false);
                    finish();
                } else {
                    // Handle failure
                }
            }
        });

        // Add TextWatcher to handle "I want to" logic
        longTermGoalInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String currentText = longTermGoalInput.getText().toString().trim();
                if (currentText.isEmpty()) {
                    // If the user clicked and it's empty, set to "I want to"
                    longTermGoalInput.setText(DEFAULT_GOAL_PREFIX);
                    longTermGoalInput.setSelection(longTermGoalInput.getText().length()); // Move cursor to the end
                }
            } else {
                // If focus is lost and no text was typed, reset to the default hint
                String currentText = longTermGoalInput.getText().toString().trim();
                if (currentText.equals(DEFAULT_GOAL_PREFIX)) {
                    longTermGoalInput.setText(""); // Reset to the hint if no input
                }
            }
        });

        longTermGoalInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Get the current text in the EditText
                String input = longTermGoalInput.getText().toString();

                // Only modify the text if it starts with "I want to" and prevent the infinite loop
                if (input.startsWith(DEFAULT_GOAL_PREFIX)) {
                    // Avoid resetting the text if it is already in the desired format
                    if (!input.equals(DEFAULT_GOAL_PREFIX)) {
                        longTermGoalInput.removeTextChangedListener(this); // Temporarily remove the TextWatcher
                        longTermGoalInput.setText(input);  // Allow the user to type after "I want to"
                        longTermGoalInput.setSelection(input.length()); // Keep the cursor at the end
                        longTermGoalInput.addTextChangedListener(this); // Reattach the TextWatcher
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });


        // Handle Submit Button Click
        submitButton.setOnClickListener(v -> {
            goal = longTermGoalInput.getText().toString().trim();
            age = ageInput.getText().toString().trim();

            // Get the selected gender from the RadioGroup
            int selectedGenderId = genderInput.getCheckedRadioButtonId();
            if (selectedGenderId == R.id.male_radio_button) {
                gender = "male";
            } else if (selectedGenderId == R.id.female_radio_button) {
                gender = "female";
            } else if (selectedGenderId == R.id.neutral_radio_button) {
                gender = "neutral"; // Handle gender-neutral option
            } else {
                gender = ""; // No gender selected
            }

            if (goal.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields before submitting.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show Confirmation Dialog
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Submission")
                    .setMessage("Are you sure you want to submit this as your long-term goal? It cannot be changed later.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        showLoading(true);
                        questionsViewModel.fetchQuestions(goal, age, gender); // Pass age and gender to ViewModel
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

    private void saveLongTermGoal(String goal) {
        // Save the goal in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("long_term_goal", goal);
        editor.apply();
    }
}
