package ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import persistance.Cue;
import persistance.CueRepository;
import util.EftApi;

public class ReflectionViewModel extends AndroidViewModel {
    private CueRepository cueRepository;
    private LiveData<Cue> cueLiveData;
    private EftApi eftApi;
    private MutableLiveData<Boolean> isAnswerCorrect = new MutableLiveData<>();

    public ReflectionViewModel(@NonNull Application application) {
        super(application);
        cueRepository = new CueRepository(application);

        // Initialize EftApi with a userId (you can pass this via a method)
        int userId = 1; // Replace with actual userId
        eftApi = new EftApi(new EftApi.ApiResponseCallback() {
            @Override
            public void onSuccess(String[] titleAndText, byte[] audioData, byte[] image) {
                // Not used for retention request
            }

            @Override
            public void onSuccess() {
                // Retention request succeeded
                Log.d("ReflectionViewModel", "Retention data sent successfully");
            }

            @Override
            public void onError(Exception e) {
                // Retention request failed
                Log.e("ReflectionViewModel", "Failed to send retention data", e);
            }
        }, userId);
    }

    public void loadCue(int cueId) {
        cueLiveData = cueRepository.getCue(cueId);
    }

    public LiveData<Cue> getCue() {
        return cueLiveData;
    }

    public void checkAnswer(String selectedAnswer, String correctAnswer) {
        String selectedLetter = selectedAnswer.substring(0, 1);
        boolean isCorrect = selectedLetter.equalsIgnoreCase(correctAnswer.substring(correctAnswer.length() - 1));
        isAnswerCorrect.setValue(isCorrect);

        if (isCorrect) {
            markCueAsRead(cueLiveData.getValue().getId());
            sendRetentionRequest();
        }
    }

    private void markCueAsRead(int cueId) {
        cueRepository.updateReadStatus(cueId, true);
    }

    private void sendRetentionRequest() {
        eftApi.requestRetention();
    }

    public LiveData<Boolean> getIsAnswerCorrect() {
        return isAnswerCorrect;
    }
}
