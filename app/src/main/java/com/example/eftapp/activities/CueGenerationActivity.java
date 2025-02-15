package com.example.eftapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.eftapp.R;

import java.util.ArrayList;

import ViewModel.ApiViewModel;
import util.InfoPopup;
import util.PollManager;

public class CueGenerationActivity extends AppCompatActivity {

    private EditText inputFieldWhere, inputFieldWhen, inputFieldWhat, inputFieldHow, inputFieldWho;
    private Button submitButton;
    private ApiViewModel apiViewModel;
    private ImageView whereQ, whenQ, howQ, whatQ, whoQ, backButton;
    private TextView displayGoal;

    private TextView loadingMessage;
    private Handler handler;
    private Runnable changeMessage;
    TextView instructionsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cue_generation);

        // Initialize UI components
        inputFieldWhere = findViewById(R.id.input_field_where);
        inputFieldWhen = findViewById(R.id.input_field_when);
        inputFieldWhat = findViewById(R.id.input_field_what);
        inputFieldHow = findViewById(R.id.input_field_how);
        inputFieldWho = findViewById(R.id.input_field_who);
        submitButton = findViewById(R.id.submit_button);

        whereQ = findViewById(R.id.question_mark_where);
        whenQ = findViewById(R.id.question_mark_when);
        howQ = findViewById(R.id.question_mark_how);
        whatQ = findViewById(R.id.question_mark_what);
        whoQ = findViewById(R.id.question_mark_who);

        backButton = findViewById(R.id.back_button);

        displayGoal = findViewById(R.id.display_goal);

        instructionsText = findViewById(R.id.instructions_text);
        String text = getString(R.string.cueInstruction);
        setIconText(text);

        //LoadingMessage
        loadingMessage = findViewById(R.id.loadingMessage);
        String[] messages = {"Generating...", "How's your day?", "Nearly finished!", "Hang tight!"};
        handler = new Handler();

        changeMessage = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                loadingMessage.setText(messages[index]);
                index = (index + 1) % messages.length;
                handler.postDelayed(this, 2000); // Update message every 2 seconds
            }
        };

        // Initialize Popup component
        InfoPopup infoPopup = new InfoPopup(this);

        // Disable the button initially
        submitButton.setEnabled(false);

        displayGoal.setText(getGoal());


        // Initialize ViewModel
        apiViewModel = new ViewModelProvider(this).get(ApiViewModel.class);

        backButton.setOnClickListener(view -> {
            finish();
        });

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

        // Set click listeners
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
        inputs.add(inputFieldHow.getText().toString());
        inputs.add(inputFieldWhat.getText().toString());
        inputs.add(inputFieldWho.getText().toString());
        inputs.add(getGoal());

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
                && !inputFieldWho.getText().toString().trim().isEmpty();

        // Enable or disable the submit button based on the result
        submitButton.setEnabled(allFieldsFilled);
    }

    private void showLoading(boolean isLoading) {
        LinearLayout loadingOverlay = findViewById(R.id.loadingOverlay);
        if (isLoading) {
            // Start changing messages when loading begins
            handler.post(changeMessage);

            loadingOverlay.setVisibility(View.VISIBLE); // Show overlay
            submitButton.setEnabled(false); // Disable the button
        } else {
            loadingOverlay.setVisibility(View.GONE); // Hide overlay
            submitButton.setEnabled(true); // Enable the button
        }
    }

    private String getGoal() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getString("long_term_goal", null);
    }

    private void setIconText(String text) {
        // Find the position of the word "here"
        int start = text.indexOf("here");
        int end = start + "here".length();

        // Create a SpannableString from the text
        SpannableString spannableString = new SpannableString(text);

        // Load the drawable
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_question_mark); // Replace with your icon
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()); // Set bounds for the drawable

        // Create an ImageSpan with the drawable
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

        // Replace "here" with the icon
        spannableString.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString to the TextView
        instructionsText.setText(spannableString);
    }
}
