package ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eftapp.R;

import java.util.ArrayList;
import java.util.List;

import util.Poll;
import util.PollApi;
import util.PollManager;
import util.UserManager;

public class PollViewModel extends AndroidViewModel implements PollApi.PollApiCallback {

    private MutableLiveData<Poll> currentPoll = new MutableLiveData<>();
    private MutableLiveData<String> feedbackText = new MutableLiveData<>();
    private ArrayList<Double> pollResults = new ArrayList<>();
    private MutableLiveData<Boolean> isPollSessionComplete = new MutableLiveData<>(false);  // New LiveData to track session end
    private List<Poll> pollQueue = new ArrayList<>();
    private final MutableLiveData<Boolean> pollApiResult = new MutableLiveData<>();
    private int userId;
    private int currentPollIndex = 0;
    private String endPollText;

    public PollViewModel(Application application) {
        super(application);
        UserManager userManager = new UserManager(application);
        this.userId = userManager.getUserId();

        endPollText = application.getString(R.string.endPollText);
        startPollSession(application);
    }

    public LiveData<Poll> getCurrentPoll() {
        return currentPoll;
    }

    public LiveData<String> getFeedbackText() {
        return feedbackText;
    }

    public LiveData<Boolean> isPollSessionComplete() { // Getter for the new LiveData
        return isPollSessionComplete;
    }

    private void startPollSession(Application application) {
        PollManager pollManager = new PollManager(application);

        // Add polls to the queue
        pollQueue.add(pollManager.createPoll("AdjustingAmount"));
        pollQueue.add(pollManager.createPoll("Belohnungsaufschub"));
        pollQueue.add(pollManager.createPoll("TSRQ"));

        // Start with the first poll
        if (!pollQueue.isEmpty()) {
            currentPoll.setValue(pollQueue.get(currentPollIndex));
        }
    }

    public void handleAnswer(int answerIndex) {
        Poll poll = currentPoll.getValue();
        if (poll == null || poll.isComplete()) return;

        poll.handleAnswer(answerIndex);

        // Update feedback when poll is complete
        if (poll.isComplete()) {
            // Move to the next poll
            currentPollIndex++;
            double results[] = poll.getTotalScore();

            for (double value : results) {
                pollResults.add(value);
            }

            if (currentPollIndex < pollQueue.size()) {
                currentPoll.setValue(pollQueue.get(currentPollIndex));
            } else {
                String resultsList = "";
                for (Double d : pollResults) {
                    resultsList += d;
                    resultsList += "\n";
                }

                feedbackText.setValue(endPollText);
                isPollSessionComplete.setValue(true);
            }
        } else {
            currentPoll.setValue(poll); // Update poll state
        }
    }

    public void sendPollResultToBackend() {
        PollApi pollApi = new PollApi(this);
        pollApi.submitPoll(pollResults, userId);
    }

    public LiveData<Boolean> fetchApiCallLiveData() {
        return pollApiResult;
    }

    public void setPollDate() {
        PollManager.saveFirstPollDate(getApplication());
    }

    @Override
    public void onSuccess() {
        pollApiResult.postValue(true);
    }

    @Override
    public void onError(Exception e) {
        pollApiResult.postValue(false);
    }
}
