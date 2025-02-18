package util;

import android.content.Context;
import android.util.Log;

import java.io.File;

import persistance.Cue;

public class FileUtils {

    /**
     * Deletes the audio and image files associated with a Cue.
     *
     * @param context The context used to access the file system.
     * @param cue     The Cue whose files need to be deleted.
     */
    public static void deleteCueFiles(Context context, Cue cue) {
        if (cue.getVoicePath() != null) {
            File audioFile = new File(cue.getVoicePath());
            if (audioFile.exists()) {
                boolean deleted = audioFile.delete();
                if (deleted) {
                    Log.d("FileUtils", "Deleted audio file: " + cue.getVoicePath());
                } else {
                    Log.e("FileUtils", "Failed to delete audio file: " + cue.getVoicePath());
                }
            }
        }

        if (cue.getImagePath() != null) {
            File imageFile = new File(cue.getImagePath());
            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                if (deleted) {
                    Log.d("FileUtils", "Deleted image file: " + cue.getImagePath());
                } else {
                    Log.e("FileUtils", "Failed to delete image file: " + cue.getImagePath());
                }
            }
        }
    }
}
