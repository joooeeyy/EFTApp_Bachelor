package com.example.eftapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eftapp.R;

import java.util.ArrayList;
import java.util.List;

import ViewModel.CueViewModel;
import persistance.Cue;
import util.CueAdapter;

public class HomeFragment extends Fragment implements CueAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private CueAdapter cueAdapter;
    private List<Cue> cueList = new ArrayList<>();  // Initialize with an empty list
    private CueViewModel cueViewModel;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Set up the Toolbar
        androidx.appcompat.widget.Toolbar toolbar = rootView.findViewById(R.id.home_toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            // Use ContextCompat to get the color
            toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            toolbar.setTitle("Mental Movie Hub");
        }

        // Find the RecyclerView in the fragment layout
        recyclerView = rootView.findViewById(R.id.cueRecyclerView);

        // Initialize the CueAdapter with the empty list
        cueAdapter = new CueAdapter(cueList, getContext(), cueId -> {
            Intent intent = new Intent(getActivity(), ReflectionActivity.class);
            intent.putExtra("cueId", cueId);
            startActivity(intent);
        }, cueId -> {
            cueViewModel.getCue(cueId).observe(getViewLifecycleOwner(), cue -> {
                if (cue != null) {
                    cueViewModel.deleteCue(cue); // This will now delete the files as well
                }
            });
        });

        recyclerView.setAdapter(cueAdapter);

        // Set up the LinearLayoutManager with reverse order
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);  // This will reverse the order
        layoutManager.setStackFromEnd(true);   // Ensures that the last item is at the bottom initially
        recyclerView.setLayoutManager(layoutManager);

        // Initialize ViewModel
        cueViewModel = new ViewModelProvider(this).get(CueViewModel.class);

        // Observe LiveData from ViewModel
        cueViewModel.getAllCues().observe(getViewLifecycleOwner(), new Observer<List<Cue>>() {
            @Override
            public void onChanged(List<Cue> newCueList) {
                if (newCueList != null) {
                    // Update the adapter with the new list of cues
                    cueAdapter.updateCueList(newCueList);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onItemClick(int id) {
        // Handle item click
    }
}