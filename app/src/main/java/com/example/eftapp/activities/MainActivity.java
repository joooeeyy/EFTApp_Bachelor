package com.example.eftapp.activities;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.eftapp.R;
import com.example.eftapp.databinding.ActivityMainBinding;

import util.UserManager;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize binding and set it as the content view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize UserManager and check if the user ID exists, if not generate it
        UserManager userManager = new UserManager(this);

        // Check if the user ID is -1 (meaning it doesn't exist)
        if (userManager.getUserId() == -1) {
            userManager.generateAndSaveUserId();  // Generate and save a new user ID
        }


        // Set the action bar title
        setTitle("Add Cue"); // Initial title

        binding.bottomNavigationView.setSelectedItemId(R.id.add); //

        // Set initial fragment
        replaceFragment(new AddCueFragment(), "Add Cue");
        binding.bottomNavigationView.setBackground(null);

        // Set up BottomNavigationView item selection listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            String title = "";

            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                selectedFragment = new HomeFragment();
                title = "Home";
            } else if (itemId == R.id.add) {
                selectedFragment = new AddCueFragment();
                title = "Add Cue";
            } else if (itemId == R.id.settings) {
                selectedFragment = new SettingsFragment();
                title = "Settings";
            } else {
                return false;
            }

            replaceFragment(selectedFragment, title);
            return true;
        });
    }

    private void replaceFragment(Fragment fragment, String title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

        // Set the action bar title
        setTitle(title);
    }
}
