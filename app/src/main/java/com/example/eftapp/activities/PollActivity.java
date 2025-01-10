package com.example.eftapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eftapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.AdjustingAmountPoll;
import util.BelohnungsaufschubPoll;
import util.Poll;
import util.PollManager;
import util.TSRQPoll;

public class PollActivity extends AppCompatActivity {

    private TextView questionText, feedbackText;
    private Button[] answerButtons = new Button[7]; // Update to 7 buttons

    private List<Poll> pollQueue;
    private int currentPollIndex = 0;
    private Poll currentPoll;

    // Store final results here
    private ArrayList<String> pollResults = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_poll);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedGoal = preferences.getString("questions", null);

        questionText = findViewById(R.id.question);
        feedbackText = findViewById(R.id.feedback);

        // Initialize the 7 answer buttons
        answerButtons[0] = findViewById(R.id.btn_left);
        answerButtons[1] = findViewById(R.id.btn_middle_left);
        answerButtons[2] = findViewById(R.id.btn_middle);
        answerButtons[3] = findViewById(R.id.btn_middle_right);
        answerButtons[4] = findViewById(R.id.btn_right);
        answerButtons[5] = findViewById(R.id.btn_right_more);
        answerButtons[6] = findViewById(R.id.btn_right_most);

        PollManager pollManager = new PollManager(this);

        // Add polls to queue
        pollQueue = new ArrayList<>();
        pollQueue.add(pollManager.createPoll("AdjustingAmount"));
        pollQueue.add(pollManager.createPoll("Belohnungsaufschub"));
        pollQueue.add(pollManager.createPoll("TSRQ"));

        // Start with the first poll
        currentPollIndex = 0;
        currentPoll = pollQueue.get(currentPollIndex);
        updateUI();

        // Set onClickListeners for the answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int index = i;
            answerButtons[i].setOnClickListener(view -> handleAnswer(index));
        }
    }

    private void handleAnswer(int answerIndex) {
        if (currentPoll == null || currentPoll.isComplete()) return;

        currentPoll.handleAnswer(answerIndex);

        if (currentPoll.isComplete()) {
            // Save the final result for the completed poll
            pollResults.add(

                    currentPoll.getFeedback() // Save final feedback as result
            );

            feedbackText.setText("Poll Complete: " + currentPoll.getFeedback());
            currentPollIndex++;
            if (currentPollIndex < pollQueue.size()) {
                currentPoll = pollQueue.get(currentPollIndex);
                updateUI();
            } else {
                endPollingSession();
            }
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        if (currentPoll != null) {
            questionText.setText(currentPoll.getCurrentQuestion());
            feedbackText.setText(currentPoll.getFeedback());

            String[] answers = currentPoll.getAnswerChoices();
            for (int i = 0; i < answerButtons.length; i++) {
                if (i < answers.length) {
                    answerButtons[i].setVisibility(View.VISIBLE);
                    answerButtons[i].setText(answers[i]);
                } else {
                    answerButtons[i].setVisibility(View.GONE);
                }
            }
        }
    }

    private void endPollingSession() {
        questionText.setText("Thank you for completing all polls!");

        String results = "";
        for (Poll p : pollQueue) {
            results += p.getPollMethod() + ": ";
            for (double d : p.getTotalScore()){
                results += d + " ";
            }
            results += "\n";
        }

        feedbackText.setText("Your responses have been recorded. Results are: "  +
                results);

        for (Button button : answerButtons) {
            button.setVisibility(View.GONE);
        }
    }
}
