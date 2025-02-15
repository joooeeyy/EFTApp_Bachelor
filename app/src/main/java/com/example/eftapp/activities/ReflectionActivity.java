package com.example.eftapp.activities;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.eftapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ViewModel.ReflectionViewModel;
import persistance.Cue;
import util.PollManager;
import util.SimpleAudioPlayer;

public class ReflectionActivity extends AppCompatActivity {
    private ReflectionViewModel reflectionViewModel;
    private SimpleAudioPlayer audioPlayer;
    private SeekBar audioSeekBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBarTask;
    private int cueId;
    private ImageView backButton;

    private AlertDialog storyDialog;
    private AlertDialog questionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reflection);

        reflectionViewModel = new ViewModelProvider(this).get(ReflectionViewModel.class);

        audioPlayer = new SimpleAudioPlayer(this);
        audioSeekBar = findViewById(R.id.audioSeekBar);
        seekBarHandler = new Handler();
        backButton = findViewById(R.id.back_button);

        // Reset future events if needed (e.g., at midnight)
        PollManager.resetFutureEventsIfNeeded(this);

        cueId = getIntent().getIntExtra("cueId", -1);
        reflectionViewModel.loadCue(cueId);

        reflectionViewModel.getCue().observe(this, cue -> {
            if (cue != null) {
                try {
                    byte[] audio = readFile(cue.getVoicePath());
                    byte[] image = readFile(cue.getImagePath());
                    displayImage(image, findViewById(R.id.cueImageView));
                } catch (IOException e) {
                    Log.e("ReflectionActivity", "Error reading files", e);
                    Toast.makeText(this, "Error loading cue data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Cue not found", Toast.LENGTH_SHORT).show();
            }
        });

        reflectionViewModel.getIsAnswerCorrect().observe(this, isCorrect -> {
            if (isCorrect != null) {
                if (isCorrect) {
                    Toast.makeText(this, "Correct! Well done!", Toast.LENGTH_SHORT).show();

                    // Update future events left
                    int futureEventsLeft = PollManager.getFutureEventsLeft(this);
                    if (futureEventsLeft > 0) {
                        PollManager.setFutureEventsLeft(this, futureEventsLeft - 1);
                    }

                    finish();
                } else {
                    Toast.makeText(this, "Incorrect answer. Please listen again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update SeekBar periodically
        updateSeekBarTask = new Runnable() {
            @Override
            public void run() {
                if (audioPlayer.isPlaying()) {
                    audioSeekBar.setProgress(audioPlayer.getCurrentPosition());
                    seekBarHandler.postDelayed(this, 100); // Update every 100ms
                }
            }
        };

        // SeekBar change listener
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHandler.removeCallbacks(updateSeekBarTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarHandler.post(updateSeekBarTask);
            }
        });

        backButton.setOnClickListener(view -> finish());
    }

    public void displayImage(byte[] imageData, ImageView imageView) {
        if (imageData != null && imageData.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            imageView.setImageBitmap(bitmap);
        } else {
            Log.d("bigerror", "NO IMAGE FOUND");
        }
    }

    public void play(View view) {
        Cue cue = reflectionViewModel.getCue().getValue();
        if (cue != null && !audioPlayer.isPlaying()) {
            try {
                byte[] audio = readFile(cue.getVoicePath());
                if (audio != null) {
                    // Show story text popup when play starts
                    showStoryPopup(cue.getText());

                    audioPlayer.playAudio(audio, mp -> {
                        runOnUiThread(() -> {
                            // Dismiss story dialog if still showing
                            if (storyDialog != null && storyDialog.isShowing()) {
                                storyDialog.dismiss();
                            }

                            // Show question popup when audio finishes
                            showQuestionPopup(cue.getQuestion(), cue.getAnswers(), cue.getSolution());
                        });
                    });

                    // Delay SeekBar setup to ensure MediaPlayer is prepared
                    new Handler().postDelayed(() -> {
                        int duration = audioPlayer.getDuration();
                        if (duration > 0) {
                            audioSeekBar.setMax(duration);
                            seekBarHandler.post(updateSeekBarTask);
                        } else {
                            Toast.makeText(this, "Failed to get audio duration", Toast.LENGTH_SHORT).show();
                        }
                    }, 500); // Allow time for preparation
                } else {
                    Toast.makeText(this, "No audio data available", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e("ReflectionActivity", "Error reading audio file", e);
                Toast.makeText(this, "Error loading audio", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Audio is already playing", Toast.LENGTH_SHORT).show();
        }
    }

    public void pause(View view) {
        if (audioPlayer.isPlaying()) {
            audioPlayer.pauseAudio();
            Log.d("ReflectionActivity", "Audio paused.");
        } else {
            Toast.makeText(this, "Audio is not playing", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        seekBarHandler.removeCallbacks(updateSeekBarTask);
        audioPlayer.releasePlayer();
    }

    private void showStoryPopup(String storyText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_story, null);
        TextView storyTextView = dialogView.findViewById(R.id.storyTextView);

        storyTextView.setText(storyText);

        builder.setView(dialogView)
                .setTitle("Story")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        storyDialog = builder.create();
        storyDialog.show();
    }

    private void showQuestionPopup(String question, String answers, String solution) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Question: " + question)
                .setCancelable(false); // Prevent dismissing by clicking outside

        // Split answers into individual options
        String[] answerOptions = answers.split("\n");

        // Inflate custom layout for buttons
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_question, null);
        LinearLayout answersLayout = dialogView.findViewById(R.id.answersLayout);

        // Create answer buttons
        for (String option : answerOptions) {
            Button btn = new Button(this);
            btn.setText(option);
            btn.setOnClickListener(v -> handleAnswerSelection(option, solution));
            answersLayout.addView(btn);
        }

        builder.setView(dialogView);
        questionDialog = builder.create(); // Assign the dialog to the class variable
        questionDialog.show();
    }

    private void handleAnswerSelection(String selectedAnswer, String solution) {
        // Extract just the letter (A/B/C/D) from solution
        String correctAnswer = solution.substring(solution.length() - 1);
        String selectedLetter = selectedAnswer.substring(0, 1);

        if (selectedLetter.equalsIgnoreCase(correctAnswer)) {
            // Correct answer
            reflectionViewModel.checkAnswer(selectedAnswer, solution);
        } else {
            // Incorrect answer
            Toast.makeText(this, "Incorrect answer. Please listen again.", Toast.LENGTH_SHORT).show();
            if (questionDialog != null && questionDialog.isShowing()) {
                questionDialog.dismiss(); // Dismiss the question dialog
            }
        }
    }

    private byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        }
        return data;
    }
}