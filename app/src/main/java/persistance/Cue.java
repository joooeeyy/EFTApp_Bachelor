package persistance;

import androidx.room.Entity;
import androidx.room.Ignore;
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

    @ColumnInfo(name = "voice_path")
    public String voicePath; // Store the file path for the audio file

    @ColumnInfo(name = "isRead")
    public boolean isRead = false;

    @ColumnInfo(name = "image_path")
    public String imagePath; // Store the file path for the image file

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "answers")
    public String answers;

    @ColumnInfo(name = "solution")
    public String solution;

    // Default constructor (needed by Room)
    @Ignore
    public Cue() {
        // Required by Room
    }

    // Additional constructors
    public Cue(String title, String text, String question, String answers, String solution, String voicePath, String imagePath) {
        this.title = title;
        this.text = text;
        this.voicePath = voicePath;
        this.imagePath = imagePath;
        this.question = question;
        this.answers = answers;
        this.solution = solution;
    }

    // Getters and setters
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

    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}