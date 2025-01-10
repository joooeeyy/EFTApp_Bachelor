package util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SimpleAudioPlayer {

    private MediaPlayer mediaPlayer;
    private Context context;
    private boolean isPrepared = false;
    private boolean isPaused = false;
    private int lastPosition = 0; // Keeps track of where the audio was paused

    public SimpleAudioPlayer(Context context) {
        this.context = context;
        mediaPlayer = new MediaPlayer();
    }

    public void playAudio(byte[] audioData, MediaPlayer.OnCompletionListener onCompletionListener) {
        if (isPaused) {
            // Resume from the last paused position
            mediaPlayer.seekTo(lastPosition);
            mediaPlayer.start();
            Log.d("SimpleAudioPlayer", "Resuming audio from position: " + lastPosition);
            isPaused = false;
        } else {
            // First time playing the audio, prepare the player
            File tempFile = null;
            FileOutputStream fos = null;

            try {
                tempFile = File.createTempFile("audio", ".mp3", context.getCacheDir());
                fos = new FileOutputStream(tempFile);
                fos.write(audioData);

                mediaPlayer.reset();
                mediaPlayer.setDataSource(tempFile.getAbsolutePath());
                mediaPlayer.setOnPreparedListener(mp -> {
                    isPrepared = true;
                    mediaPlayer.start();
                    Log.d("SimpleAudioPlayer", "Audio playback started.");
                });
                // Set the onCompletionListener provided by the calling activity
                mediaPlayer.setOnCompletionListener(onCompletionListener);
                mediaPlayer.prepareAsync(); // Prepare asynchronously
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("SimpleAudioPlayer", "Error playing audio from byte array", e);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(position);
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            lastPosition = mediaPlayer.getCurrentPosition(); // Store the position before pausing
            mediaPlayer.pause();
            isPaused = true;
            Log.d("SimpleAudioPlayer", "Audio paused at position: " + lastPosition);
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
