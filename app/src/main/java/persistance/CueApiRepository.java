package repository;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import ViewModel.ApiViewModel;
import persistance.Cue;
import persistance.CueDao;
import persistance.CueDatabase;
import util.EftApi;
import util.SettingsManager;
import util.UserManager;

public class CueApiRepository implements EftApi.ApiResponseCallback {

    private EftApi eftApi;
    private CueDao cueDao;
    private ApiViewModel apiViewModel;
    private Context context;
    private String voiceSetting;

    public CueApiRepository(Context context, ApiViewModel apiViewModel) {
        CueDatabase cueDatabase = CueDatabase.getInstance(context);
        this.cueDao = cueDatabase.cueDao();
        UserManager userManager = new UserManager(context);
        int userId = userManager.getUserId();

        SettingsManager settingsManager = new SettingsManager(context);
        voiceSetting = settingsManager.getValue();

        this.eftApi = new EftApi(this, userId);
        this.apiViewModel = apiViewModel;
        this.context = context;
    }

    public void fetchCueData(ArrayList<String> inputs) {
        eftApi.aiTextResponse(inputs, voiceSetting);
    }

    public void randomiseCue(String goal) {
        eftApi.requestRandomise(goal);
    }

    @Override
    public void onSuccess(String[] cueResponse, byte[] audioData, byte[] image) {
        String title = cueResponse[0];
        String text = cueResponse[1];
        String question = cueResponse[2];
        String answers = cueResponse[3];
        String solution = cueResponse[4];

        try {
            // Save audio and image to files
            String audioFilePath = saveAudioToFile(audioData);
            String imageFilePath = saveImageToFile(image);

            // Insert cue with file paths
            Cue cue = new Cue(title, text, question, answers, solution, audioFilePath, imageFilePath);
            cueDao.insert(cue);
            apiViewModel.setApiResponse(true);
        } catch (IOException e) {
            Log.e("CueApiRepository", "Error saving files", e);
            apiViewModel.setApiResponse(false);
        }
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(Exception e) {
        Log.d("ResponseCallbackError", "CueApi Response Error");
        apiViewModel.setApiResponse(false);
    }

    @Override
    public void onSuccess(String randomizedCue) {
        apiViewModel.setRandomizedCueResponse(randomizedCue);
    }

    private String saveAudioToFile(byte[] audioData) throws IOException {
        File audioFile = new File(context.getFilesDir(), "audio_" + UUID.randomUUID().toString() + ".mp3");
        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            fos.write(audioData);
        }
        return audioFile.getAbsolutePath();
    }

    private String saveImageToFile(byte[] imageData) throws IOException {
        File imageFile = new File(context.getFilesDir(), "image_" + UUID.randomUUID().toString() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(imageData);
        }
        return imageFile.getAbsolutePath();
    }
}