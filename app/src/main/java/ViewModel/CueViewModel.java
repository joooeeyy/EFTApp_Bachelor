package ViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import persistance.Cue;
import persistance.CueRepository;
import util.FileUtils;

public class CueViewModel extends AndroidViewModel {
    private CueRepository repository;
    private LiveData<List<Cue>> allCues;

    public CueViewModel(@NonNull Application application) {
        super(application);
        repository = new CueRepository(application);
        allCues = repository.getAllCues(); // Use the correct method name
    }

    public LiveData<List<Cue>> getAllCues() {
        return allCues;
    }

    public LiveData<Cue> getCue(int id) {
        return repository.getCue(id); // Use the correct method name
    }

    public void insert(Cue cue) {
        repository.insertCue(cue); // Use the correct method name
    }

    public void updateCue(Cue cue) {
        repository.updateCue(cue); // Use the correct method name
    }

    public void deleteCue(Cue cue) {
        // Delete the associated files before deleting the Cue from the database
        FileUtils.deleteCueFiles(getApplication(), cue);
        repository.deleteCue(cue); // Use the correct method name
    }

    public void updateReadStatus(int id, boolean isRead) {
        repository.updateReadStatus(id, isRead); // Use the correct method name
    }

    public LiveData<List<Cue>> getCuesByReadStatus(boolean isRead) {
        return repository.getCuesByReadStatus(isRead); // Use the correct method name
    }
}