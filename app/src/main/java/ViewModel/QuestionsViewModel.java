package ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import util.QuestionsApi;

public class QuestionsViewModel extends AndroidViewModel {
    private com.example.eftapp.util.QuestionsRepository repository;
    private final MutableLiveData<Boolean> questionsLiveData = new MutableLiveData<>();


    public QuestionsViewModel(Application application) {
        super(application);
        repository = new com.example.eftapp.util.QuestionsRepository(application);
    }

    // Method to request questions based on the long-term goal
    public void fetchQuestions(String goal) {
        repository.getAiQuestions(goal, new QuestionsApi.ApiResponseCallback() {
            @Override
            public void onSuccess(String questions) {
                // Store the questions in SharedPreferences
                repository.saveQuestionsToSharedPreferences(questions);
                questionsLiveData.postValue(true); // Update the live data
            }

            @Override
            public void onError(Exception e) {
                questionsLiveData.postValue(false);
            }
        });
    }

    public LiveData<Boolean> fetchQuestionsLiveData() {
        return questionsLiveData;
    }
}
