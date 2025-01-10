package com.example.eftapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.eftapp.R;

import ViewModel.CueViewModel;
import persistance.Cue;
import util.SimpleAudioPlayer;

public class ReflectionActivity extends AppCompatActivity {
    private CueViewModel cueViewModel;
    private byte[] audio;
    private byte[] image;
    private SimpleAudioPlayer audioPlayer;
    private SeekBar audioSeekBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBarTask;
    private int cueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reflection);

        audioPlayer = new SimpleAudioPlayer(this);
        audioSeekBar = findViewById(R.id.audioSeekBar);
        seekBarHandler = new Handler();

        int cueId = getIntent().getIntExtra("cueId", -1);
        cueViewModel = new ViewModelProvider(this).get(CueViewModel.class);

        cueViewModel.getCue(cueId).observe(this, cue -> {
            if (cue != null) {
                this.cueId = cueId;
                audio = cue.getVoice();
                image = cue.getImage();

                // Call the displayImage function
                ImageView imageView = findViewById(R.id.cueImageView);
                displayImage(image, imageView);
            } else {
                Toast.makeText(this, "Cue not found", Toast.LENGTH_SHORT).show();
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


    }



    public void displayImage(byte[] imageData, ImageView imageView) {
        if (imageData != null && imageData.length > 0) {
            // Decode the byte array to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

            // Set the Bitmap to the ImageView
            imageView.setImageBitmap(bitmap);
        } else {
            Log.d("bigerror", "NO IMAGE FOUND");
            // Handle case where imageData is null or empty
            // Optional placeholder
        }
    }



    public void play(View view) {
        if (audio != null) {
            if (!audioPlayer.isPlaying()) {
                audioPlayer.playAudio(audio, mp -> {
                    cueViewModel.updateReadStatus(cueId, true); // Update the Cue object in the database
                    Log.d("ReflectionActivity", "Audio finished, setting isRead to true.");
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
                Toast.makeText(this, "Audio is already playing", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No audio data available", Toast.LENGTH_SHORT).show();
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
}
