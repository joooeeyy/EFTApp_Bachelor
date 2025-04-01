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

import java.util.Calendar;

public class AddCueFragment extends Fragment {

    private ActivityResultLauncher<Intent> cueActivityLauncher;
    private SharedPreferences preferences;
    private boolean isGoalSet;
    private boolean isPollCompleted;
    private View aiOption, pollOption, goalOption;
    private TextView cuesLeftText, daysTillPollText, dayCountText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_cue, container, false);

        // Find views
        aiOption = view.findViewById(R.id.ai_option);
        pollOption = view.findViewById(R.id.poll_option);
        goalOption = view.findViewById(R.id.long_term_goal);
        cuesLeftText = view.findViewById(R.id.cues_left_text);
        daysTillPollText = view.findViewById(R.id.days_till_poll_text);
        dayCountText = view.findViewById(R.id.day_count_text);

        // Check the current status of goal and poll
        updateCardStatuses(view); // Pass the inflated view to updateCardStatuses

        // Update the day count as soon as the view is created
        updateDayCount();

        goalOption.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GoalActivity.class);
            startActivity(intent);
        });

        cueActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        HomeFragment homeFragment = new HomeFragment();
                        transaction.replace(R.id.frame_layout, homeFragment);
                        transaction.commit();
                    }
                }
        );

        aiOption.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CueGenerationActivity.class);
            cueActivityLauncher.launch(intent);
        });

        pollOption.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InstructionPollActivity.class);
            startActivity(intent);

            updateCardStatuses(view); // Pass the inflated view to updateCardStatuses
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        PollManager.resetFutureEventsIfNeeded(requireContext());

        // Check if the view is not null before updating UI
        View view = getView();
        if (view != null) {
            updateCardStatuses(view); // Pass the current view to updateCardStatuses
            updateDayCount(); // Update the day count whenever the fragment resumes
        }
    }

    private void updateCardStatuses(View view) {
        checkGoalStatus(); // Check if a goal is set

        // Find the container for the information cards using the passed view
        View informationCardsContainer = view.findViewById(R.id.information_cards_container);

        if (!isGoalSet) {
            // If no goal is set:
            // 1. Make the AI and Poll cards transparent and not clickable
            pollOption.setAlpha(0.5f);
            pollOption.setEnabled(false);
            aiOption.setAlpha(0.5f);
            aiOption.setEnabled(false);

            // 2. Hide the information cards container
            informationCardsContainer.setVisibility(View.GONE);

            // 3. Ensure the goal option is visible
            goalOption.setVisibility(View.VISIBLE);
        } else {
            // If a goal is set:
            // 1. Make the AI and Poll cards fully visible and clickable
            pollOption.setAlpha(1.0f);
            pollOption.setEnabled(true);
            aiOption.setAlpha(1.0f);
            aiOption.setEnabled(true);

            // 2. Show the information cards container
            informationCardsContainer.setVisibility(View.VISIBLE);

            // 3. Hide the goal option since it's already set
            goalOption.setVisibility(View.GONE);

            // 4. Update the future events and poll timers
            updateFutureEventsLeft();
            updateTimeTillNextPoll();
        }
    }

    private void checkGoalStatus() {
        SharedPreferences preferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedGoal = preferences.getString("long_term_goal", null);
        isGoalSet = savedGoal != null && !savedGoal.isEmpty();
    }

    private void updateFutureEventsLeft() {
        SharedPreferences preferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int futureEventsLeft = preferences.getInt("future_events_left", 2);
        cuesLeftText.setText("Mental Movies left for Today: " + futureEventsLeft);
    }

    private void updateTimeTillNextPoll() {
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

    private void updateDayCount() {
        long pollStartDate = PollManager.getPollStartDate(requireContext());

        if (pollStartDate == -1) {
            dayCountText.setText("DAY 1");
            return;
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(pollStartDate);

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        long diffInMillis = currentCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

        dayCountText.setText("DAY " + (diffInDays + 1));
    }
}