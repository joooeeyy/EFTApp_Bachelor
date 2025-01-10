package com.example.eftapp.activities;

import static com.example.eftapp.util.QuestionsRepository.PREFS_NAME;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.eftapp.R;

import java.util.ArrayList;

import ViewModel.ApiViewModel;
import util.InfoPopup;

public class CueGenerationActivity extends AppCompatActivity {

    private EditText inputFieldWhere, inputFieldWhen, inputFieldWhat, inputFieldHow, inputFieldWho, inputFieldSubject;
    private Button submitButton;
    private ApiViewModel apiViewModel;
    private ImageView whereQ, whenQ, howQ, whatQ, whoQ, subjectQ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Remove specific key-value pair
        editor.apply();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cue_generation);

        // Initialize UI components
        inputFieldWhere = findViewById(R.id.input_field_where);
        inputFieldWhen = findViewById(R.id.input_field_when);
        inputFieldWhat = findViewById(R.id.input_field_what);
        inputFieldHow = findViewById(R.id.input_field_how);
        inputFieldWho = findViewById(R.id.input_field_who);
        inputFieldSubject = findViewById(R.id.input_field_subject);
        submitButton = findViewById(R.id.submit_button);

        whereQ = findViewById(R.id.question_mark_where);
        whenQ = findViewById(R.id.question_mark_when);
        howQ = findViewById(R.id.question_mark_how);
        whatQ = findViewById(R.id.question_mark_what);
        whoQ = findViewById(R.id.question_mark_who);
        subjectQ = findViewById(R.id.question_mark_subject);

        // Initialize Popup component
        InfoPopup infoPopup = new InfoPopup(this);

        // Disable the button initially
        submitButton.setEnabled(false);

        // Initialize ViewModel
        apiViewModel = new ViewModelProvider(this).get(ApiViewModel.class);

        // Observe API response
        apiViewModel.getApiResponse().observe(this, success -> {
            showLoading(false);  // Hide loading spinner

            if (success != null && success) {
                // Handle success (e.g., show success message)
                Toast.makeText(CueGenerationActivity.this, "Cue saved successfully!", Toast.LENGTH_SHORT).show();

                // When you're done and want to return to HomeFragment with the result:
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);

                finish(); // Finish the activity
            } else {
                // Handle failure (e.g., show error message)
                Toast.makeText(CueGenerationActivity.this, "Failed to save Cue.", Toast.LENGTH_SHORT).show();
            }
        });

        // Add TextWatchers to all input fields
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check all fields when text changes
                checkFieldsForEmptyValues();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action required
            }
        };

        inputFieldWhere.addTextChangedListener(inputWatcher);
        inputFieldWhen.addTextChangedListener(inputWatcher);
        inputFieldWhat.addTextChangedListener(inputWatcher);
        inputFieldHow.addTextChangedListener(inputWatcher);
        inputFieldWho.addTextChangedListener(inputWatcher);
        inputFieldSubject.addTextChangedListener(inputWatcher);

        // Set click listeners
        subjectQ.setOnClickListener(v -> infoPopup.showPopup(v, getString(R.string.subject_instruction)));
        whereQ.setOnClickListener(v -> infoPopup.showPopup(v, getString(R.string.where_instruction)));
        whenQ.setOnClickListener(v -> infoPopup.showPopup(v, getString(R.string.when_instruction)));
        howQ.setOnClickListener(v -> infoPopup.showPopup(v, getString(R.string.how_instruction)));
        whatQ.setOnClickListener(v -> infoPopup.showPopup(v, getString(R.string.what_instruction)));
        whoQ.setOnClickListener(v -> infoPopup.showPopup(v, getString(R.string.who_instruction)));

    }

    public void submit(View view) {
        ArrayList<String> inputs = new ArrayList<>();
        inputs.add(inputFieldWhere.getText().toString());
        inputs.add(inputFieldWhen.getText().toString());
        inputs.add(inputFieldWhat.getText().toString());
        inputs.add(inputFieldHow.getText().toString());
        inputs.add(inputFieldWho.getText().toString());
        inputs.add(inputFieldSubject.getText().toString());

        // Show loading spinner while making the API call
        showLoading(true);

        // Make the API call to fetch text and audio
        apiViewModel.submitCue(inputs);
    }

    private void checkFieldsForEmptyValues() {
        // Check if any of the fields are empty
        boolean allFieldsFilled = !inputFieldWhere.getText().toString().trim().isEmpty()
                && !inputFieldWhen.getText().toString().trim().isEmpty()
                && !inputFieldWhat.getText().toString().trim().isEmpty()
                && !inputFieldHow.getText().toString().trim().isEmpty()
                && !inputFieldWho.getText().toString().trim().isEmpty()
                && !inputFieldSubject.getText().toString().trim().isEmpty();

        // Enable or disable the submit button based on the result
        submitButton.setEnabled(allFieldsFilled);
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
}
