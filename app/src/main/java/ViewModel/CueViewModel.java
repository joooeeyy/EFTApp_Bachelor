package ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import persistance.Cue;
import persistance.CueRepository;

public class CueViewModel extends AndroidViewModel  {

    private CueRepository cueRepository;
    private LiveData<List<Cue>> cueListLiveData;

    public CueViewModel(Application application) {
        // Initialize repository (you don't need Application context here)
        super(application);
        cueRepository = new CueRepository(application.getApplicationContext());
    }

    public LiveData<List<Cue>> getAllCues() {
        cueListLiveData = cueRepository.getAllCues();
        return cueListLiveData;
    }

    public void insertCue(Cue cue) {
        cueRepository.insertCue(cue);
    }

    public void deleteCue(Cue cue) {
        cueRepository.deleteCue(cue);
    }

    public LiveData<Cue> getCue(int id) {
        return cueRepository.getCue(id);  // Return LiveData<Cue>
    }

    public void updateReadStatus(int id, boolean isRead) {
        cueRepository.updateReadStatus(id, isRead);
    }

    public LiveData<List<Cue>> getCuesByReadStatus(boolean isRead) {
        return cueRepository.getCuesByReadStatus(isRead);
    }

    public void updateCue(Cue cue) {
        cueRepository.updateCue(cue);
    }
}

