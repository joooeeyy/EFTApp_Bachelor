package com.example.eftapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.eftapp.R;

import ViewModel.PollViewModel;
import util.Poll;
import util.PollManager;

public class PollActivity extends AppCompatActivity {

    private TextView questionText, pollFinishedText;
    private Button[] answerButtons = new Button[7]; // Update to 7 buttons
    private Button finishPollButton;  // Add the finish button
    private PollViewModel pollViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_poll);

        questionText = findViewById(R.id.question);
        pollFinishedText = findViewById(R.id.pollFinishedText);

        // Initialize answer buttons
        answerButtons[0] = findViewById(R.id.btn_left);
        answerButtons[1] = findViewById(R.id.btn_middle_left);
        answerButtons[2] = findViewById(R.id.btn_middle);
        answerButtons[3] = findViewById(R.id.btn_middle_right);
        answerButtons[4] = findViewById(R.id.btn_right);
        answerButtons[5] = findViewById(R.id.btn_right_more);
        answerButtons[6] = findViewById(R.id.btn_right_most);

        // Initialize the finish poll button
        finishPollButton = findViewById(R.id.finishPollButton);

        // Initialize ViewModel
        pollViewModel = new ViewModelProvider(this).get(PollViewModel.class);

        // Observe changes to the currentPoll
        pollViewModel.getCurrentPoll().observe(this, poll -> {
            if (poll != null) {
                questionText.setText(poll.getCurrentQuestion());
                updateAnswerButtons(poll);
            } else {
                questionText.setText("Poll FINISHED");
            }
        });

        pollViewModel.isPollSessionComplete().observe(this, isComplete -> {
            if (isComplete) {
                hideAnswerButtons();  // Hide all buttons when the poll session is complete
                questionText.setText("");
                pollFinishedText.setVisibility(View.VISIBLE);  // Show the "Poll Finished" message
                finishPollButton.setVisibility(View.VISIBLE);  // Show the finish button
            }
        });

        // Set up answer button listeners
        for (int i = 0; i < answerButtons.length; i++) {
            final int index = i;
            answerButtons[i].setOnClickListener(view -> pollViewModel.handleAnswer(index));
        }

        pollViewModel.fetchApiCallLiveData().observe(this, success -> {
            if (success) {
                showLoading(false);
                finish();
            } else {
                showLoading(false);
                Toast.makeText(getApplicationContext(), "Poll Results failed, please check " +
                        "Internet connection.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set listener for finish poll button
        finishPollButton.setOnClickListener(view -> {
            // Save the poll start date when the poll is first taken
            PollManager.savePollStartDate(this);

            // You can handle the finish action here
            pollViewModel.setPollDate();
            pollViewModel.sendPollResultToBackend();

            showLoading(true);
        });
    }

    private void updateAnswerButtons(Poll poll) {
        String[] answers = poll.getAnswerChoices();
        for (int i = 0; i < answerButtons.length; i++) {
            if (i < answers.length) {
                answerButtons[i].setVisibility(View.VISIBLE);
                answerButtons[i].setText(answers[i]);
            } else {
                answerButtons[i].setVisibility(View.GONE);
            }
        }
    }

    private void hideAnswerButtons() {
        for (Button b : answerButtons) {
            b.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean isLoading) {
        RelativeLayout loadingOverlay = findViewById(R.id.loadingOverlay);
        if (isLoading) {
            loadingOverlay.setVisibility(View.VISIBLE); // Show overlay
        } else {
            loadingOverlay.setVisibility(View.GONE); // Hide overlay
        }
    }
}