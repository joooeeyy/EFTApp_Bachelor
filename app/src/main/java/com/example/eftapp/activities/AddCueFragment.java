package com.example.eftapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.eftapp.R;

import util.PollManager;

public class AddCueFragment extends Fragment {

    private ActivityResultLauncher<Intent> cueActivityLauncher;
    private SharedPreferences preferences;
    private boolean isGoalSet;
    private boolean isPollCompleted;
    private View aiOption, pollOption, goalOption;
    private TextView cuesLeftText, daysTillPollText; // Add these variables

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_cue, container, false);

        // Find views
        aiOption = view.findViewById(R.id.ai_option);
        pollOption = view.findViewById(R.id.poll_option);
        goalOption = view.findViewById(R.id.long_term_goal);
        cuesLeftText = view.findViewById(R.id.cues_left_text); // Initialize cuesLeftText
        daysTillPollText = view.findViewById(R.id.days_till_poll_text); // Initialize daysTillPollText

        // Check the current status of goal and poll
        updateCardStatuses();

        goalOption.setOnClickListener(v -> {
            // Launch the GoalActivity to let the user set their goal
            Intent intent = new Intent(getActivity(), GoalActivity.class);
            startActivity(intent);
        });

        // Register the launcher to handle the result from CueGenerationActivity
        cueActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Ensure that AddFragment is not in the back stack and replace it with HomeFragment
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                        // Replace the AddFragment with the HomeFragment
                        HomeFragment homeFragment = new HomeFragment();

                        // Replace without adding to back stack
                        transaction.replace(R.id.frame_layout, homeFragment);

                        // Commit the transaction
                        transaction.commit();
                    }
                }
        );

        // Set an OnClickListener on the AI option card
        aiOption.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CueGenerationActivity.class);
            cueActivityLauncher.launch(intent);
        });

        // Set an OnClickListener on the Poll option card
        pollOption.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InstructionPollActivity.class);
            startActivity(intent);

            updateCardStatuses(); // Update the UI after completing the poll
        });

        return view; // Return the inflated layout
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset future events if needed (e.g., at midnight)
        PollManager.resetFutureEventsIfNeeded(requireContext());
        // Recheck the statuses whenever the fragment resumes
        updateCardStatuses();
    }

    private void updateCardStatuses() {
        // Check if the goal is set
        checkGoalStatus();

        // Check if poll should be shown again
        boolean shouldShowPoll = PollManager.shouldShowPollAgain(getContext());

        if (!isGoalSet) {
            // If the goal is not set, both Poll and Add Cue options should be at 0.5 alpha
            pollOption.setAlpha(0.5f);
            pollOption.setEnabled(false);
            aiOption.setAlpha(0.5f);
            aiOption.setEnabled(false);

            // Hide the TextViews
            cuesLeftText.setVisibility(View.GONE);
            daysTillPollText.setVisibility(View.GONE);
        } else if (isGoalSet) {
            // If goal is set, hide the goal card
            goalOption.setVisibility(View.GONE);

            // Show the TextViews
            cuesLeftText.setVisibility(View.VISIBLE);
            daysTillPollText.setVisibility(View.VISIBLE);

            // Update the TextViews with the correct values
            updateFutureEventsLeft();
            updateTimeTillNextPoll();

            if (shouldShowPoll) {
                // If poll is due, make Poll card visible and Add Cue card semi-transparent
                pollOption.setAlpha(1.0f);
                pollOption.setEnabled(true);
                aiOption.setAlpha(0.5f);
                aiOption.setEnabled(false);
            } else {
                // If poll is completed, make Add Cue card visible and Poll card semi-transparent
                pollOption.setAlpha(0.5f);
                pollOption.setEnabled(false);
                aiOption.setAlpha(1.0f);
                aiOption.setEnabled(true);
            }
        }
    }

    private void checkGoalStatus() {
        // Retrieve SharedPreferences to check if goal is set
        SharedPreferences preferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedGoal = preferences.getString("long_term_goal", null);

        // If goal is set, update the flag
        isGoalSet = savedGoal != null && !savedGoal.isEmpty();
    }

    private void updateFutureEventsLeft() {
        // Retrieve the number of future events left from SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int futureEventsLeft = preferences.getInt("future_events_left", 2); // Default to 2

        // Update the TextView
        cuesLeftText.setText("Future Events Left: " + futureEventsLeft);
    }

    private void updateTimeTillNextPoll() {
        // Calculate the time till the next poll
        long firstPollDate = PollManager.getFirstPollDate(requireContext());
        if (firstPollDate == -1) {
            daysTillPollText.setText("Next Poll: Not Scheduled");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - firstPollDate;
        long timeTillNextPoll = PollManager.ONE_WEEK_MILLIS - timeDifference;

        if (timeTillNextPoll <= 0) {
            daysTillPollText.setText("Next Poll: Due Now");
        } else {
            long daysTillNextPoll = timeTillNextPoll / (24 * 60 * 60 * 1000);
            daysTillPollText.setText("Next Poll in: " + daysTillNextPoll + " days");
        }
    }
}