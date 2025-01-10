package persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CueDao {
    @Insert
    void insert(Cue cue);

    @Delete
    void delete(Cue cue);

    @Query("SELECT * FROM cues")
    LiveData<List<Cue>> getAllResponses();

    @Query("SELECT * FROM cues WHERE id = :id")
    LiveData<Cue> getResponseById(int id);

    @Query("UPDATE cues SET isRead = :isRead WHERE id = :id")
    void updateReadStatus(int id, boolean isRead);

    @Query("SELECT * FROM cues WHERE isRead = :isRead")
    LiveData<List<Cue>> getCuesByReadStatus(boolean isRead);

    @Update
    void updateCue(Cue cue);  // This will update the Cue entity
}
