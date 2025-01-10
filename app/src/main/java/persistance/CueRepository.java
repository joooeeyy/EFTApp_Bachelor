package persistance;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import util.EftApi;

public class CueRepository {
    private CueDao cueDao;

    public CueRepository(Context context) {
        CueDatabase cueDatabase = CueDatabase.getInstance(context);
        this.cueDao = cueDatabase.cueDao();
    }

    // Get all cues from the database
    public LiveData<List<Cue>> getAllCues() {
        return cueDao.getAllResponses();
    }

    public void deleteCue(Cue cue) {
        CueDatabase.databaseWriteExecutor.execute(() -> {
            cueDao.delete(cue);
        });
    }

    // Insert cue into the database
    public void insertCue(Cue cue) {
        CueDatabase.databaseWriteExecutor.execute(() -> {
            cueDao.insert(cue);
        });
    }

    public LiveData<Cue> getCue(int id) {
        return cueDao.getResponseById(id);  // Return LiveData<Cue>
    }

    public void updateReadStatus(int id, boolean isRead) {
        CueDatabase.databaseWriteExecutor.execute(() -> cueDao.updateReadStatus(id, isRead));
    }

    public LiveData<List<Cue>> getCuesByReadStatus(boolean isRead) {
        return cueDao.getCuesByReadStatus(isRead);
    }

    public void updateCue(Cue cue) {
        CueDatabase.databaseWriteExecutor.execute(() -> cueDao.updateCue(cue));
    }
}
