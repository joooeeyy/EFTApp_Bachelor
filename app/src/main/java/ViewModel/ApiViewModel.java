package ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

public class ApiViewModel extends AndroidViewModel {

    private repository.CueApiRepository cueApiRepository;
    private final MutableLiveData<Boolean> apiResponseLiveData = new MutableLiveData<>();


    public ApiViewModel(Application application) {
        super(application);
        cueApiRepository = new repository.CueApiRepository(application.getApplicationContext(), this);
    }

    public LiveData<Boolean> getApiResponse() {
        return apiResponseLiveData;
    }

    public void submitCue(ArrayList<String> input1) {
        cueApiRepository.fetchCueData(input1);
    }

    // This method will be called by the repository when the response comes
    public void setApiResponse(boolean success) {
        apiResponseLiveData.postValue(success);
    }
}
