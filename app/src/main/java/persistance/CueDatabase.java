package persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Response;

@Database(entities = {Cue.class}, version = 3)
public abstract class CueDatabase extends RoomDatabase {
    private static CueDatabase INSTANCE;
    public abstract CueDao cueDao();

    // Executor for background database tasks
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    // Singleton pattern to get the database instance
    public static synchronized CueDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CueDatabase.class, "cue_database")
                    .fallbackToDestructiveMigration()  // Used to avoid issues when you change schema
                    .build();
        }
        return INSTANCE;
    }

    public void clearDatabase() {
        databaseWriteExecutor.execute(() -> {
            INSTANCE.clearAllTables();
        });
    }
}
