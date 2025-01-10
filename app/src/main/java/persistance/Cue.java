package persistance;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "cues")
public class Cue {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "voice")
    public byte[] voice; // Storing the MP3 file as a byte array

    @ColumnInfo(name = "isRead")
    public boolean isRead = false;

    @ColumnInfo(name = "image")
    public byte[] image; // Storing the image file as a byte array

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Add constructors, getters, and setters as needed

    // Default constructor (needed by Room)
    public Cue() {
        // Required by Room
    }

    // Additional constructors
    public Cue(String title, String text, byte[] voice, byte[] image) {
        this.title = title;
        this.text = text;
        this.voice = voice;
        this.image = image;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getVoice() {
        return voice;
    }

    public void setVoice(byte[] voice) {
        this.voice = voice;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
