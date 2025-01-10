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

import com.example.eftapp.R;


public class AddCueFragment extends Fragment {

    private ActivityResultLauncher<Intent> cueActivityLauncher;
    private SharedPreferences preferences;
    private boolean isGoalSet;
    private View aiOption, pollOption, goalOption;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_cue, container, false);

        // Find the CardView in the inflated layout
        CardView aiCardView = view.findViewById(R.id.ai_option);
        CardView pollCardView = view.findViewById(R.id.poll_option);
        CardView longTermView = view.findViewById(R.id.long_term_goal);

        // Find views
        aiOption = view.findViewById(R.id.ai_option);
        pollOption = view.findViewById(R.id.poll_option);
        goalOption = view.findViewById(R.id.long_term_goal);

        // Check if the goal is set (this will be checked in onResume)
        checkGoalStatus();

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

        // Set an OnClickListener on the CardView
        aiCardView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CueGenerationActivity.class);
            cueActivityLauncher.launch(intent);
        });

        // Set an OnClickListener on the CardView
        pollCardView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PollActivity.class);
            startActivity(intent);
        });

        return view; // Return the inflated layout
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recheck the goal status whenever the fragment resumes
        checkGoalStatus();
    }

    private void checkGoalStatus() {
        // Retrieve SharedPreferences to check if goal is set
        SharedPreferences preferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedGoal = preferences.getString("long_term_goal", null);

        // If goal is set, hide the option to set a goal, otherwise show it
        if (savedGoal != null && !savedGoal.isEmpty()) {
            goalOption.setVisibility(View.GONE);  // Goal is set, hide the button
        } else {
            goalOption.setVisibility(View.VISIBLE); // Goal is not set, show the button
        }
    }
}