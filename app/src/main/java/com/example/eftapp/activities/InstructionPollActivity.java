package com.example.eftapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eftapp.R;

public class InstructionPollActivity extends AppCompatActivity {

    private Button startPollButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instruction_poll);

        startPollButton = findViewById(R.id.startPollButton);

        startPollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the button is clicked, navigate to PollActivity
                Intent intent = new Intent(InstructionPollActivity.this, PollActivity.class);
                startActivity(intent);
                finish(); // Optionally finish the current activity to prevent navigating back
            }
        });

    }
}