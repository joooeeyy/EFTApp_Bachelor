package com.example.eftapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.eftapp.R;


public class SettingsFragment extends Fragment {

    private Button developerButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and return the layout directly
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        developerButton = view.findViewById(R.id.developerButton);

        // Set up the Toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            // Use ContextCompat to get the color
            toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            toolbar.setTitle("Voice Settings");
        }

        // Gender Spinner
        Spinner spinnerGender = view.findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Find the Language Spinner
        Spinner spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.language_options,
                android.R.layout.simple_spinner_item
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        developerButton.setOnClickListener(view1 -> {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Remove specific key-value pair
            editor.apply();
        });

        // Inflate the layout for this fragment
        return view;


    }
}