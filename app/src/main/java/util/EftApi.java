package util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eftapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EftApi {

    private static final String API_URL = "https://chagpteft.onrender.com/api";
    private static final String TTS_URL = "https://chagpteft.onrender.com/text-to-speech";
    private static final String IMAGE_URL = "https://chagpteft.onrender.com/generate-image";
    private static final String RETENTION_URL = "https://chagpteft.onrender.com/add-retention";
    private final String cueTextPrompt = "Create a 300 long word story using Easy-to-understand vocabulary and phrasing. Avoid using\n" +
            "        names at all except if it is specified in the \"who is there:\" section.\n" +
            "\n" +
            "At the end, put a trivial question regarding the first paragraph of the story. Provide:\n" +
            "        A multiple-choice question (4 possible answers: A, B, C, D), with only 1 correct answer.\n" +
            "\n" +
            "Clearly mark the correct answer at the bottom (e.g., \"Solution: B\"). Clearly mark the question.\n" +
            "        (e.g. \"Question: \".\n" +
            "\n" +
            "Provide a title for the story at the top.\n" +
            "\n" +
            "Base the story on those factors (If the answers are in German, produce the story text in German):";
    private final String cueImagePrompt = "Make a simplistic enviroment picture as motivation without using any humans or animals, which fits the following text: ";
    private int userId;

    private OkHttpClient client;
    private ApiResponseCallback callback;

    public EftApi(ApiResponseCallback callback, int userId) {
        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        this.userId = userId;
    }

    // First API call to get text response
    public void aiTextResponse(ArrayList<String> inputs, String voiceSetting) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();

        //where when what how who subject
        String prompt = cueTextPrompt + "\n Where does it take place: " + inputs.get(0) + "\n When does it take place in the future: " + inputs.get(1) +
                "\n What is the action: " + inputs.get(2) + "\n What objects are in the environment: " + inputs.get(3) + "\n Who is there: " + inputs.get(4) +
                "\n long term goal:" + inputs.get(5);

        try {
            jsonObject.put("content", prompt);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Request body for the AI Text API call
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // Make the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ApiCallback", "Failed to get text response", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    String text = jsonResponse.getString("text");

                    Log.d("CueResponse", text);

                    Log.d("ApiCallback", "Received text: " + text);

                    // Once we get the text, make the second API call to get the audio file
                    requestTextToSpeech(text, voiceSetting);

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Second API call to convert the text to speech (audio)
    private void requestTextToSpeech(String input, String voiceSetting) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();

        String[] titleAndText = splitText(input);

        String text = titleAndText[1];

        Log.d("voiceSetting", voiceSetting);

        try {
            jsonObject.put("speed", 1);
            jsonObject.put("speaker", voiceSetting);
            jsonObject.put("text", text + "\n");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(TTS_URL)
                .post(body)
                .addHeader("Accept", "application/json")  // Accept header
                .addHeader("Content-Type", "application/json")  // Content-Type header
                .build();

        // Make the TTS request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ApiCallback", "Failed to get audio response", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.body() != null) {
                        byte[] audioData = response.body().bytes(); // Get the audio data as byte array
                        Log.d("ApiCallback", "Audio data received: " + audioData.length);
                        fetchImage(titleAndText, audioData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fetchImage(String[] titleAndText, byte[] audioData) {
        String cueText = titleAndText[1];
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();

        try {
            // New JSON structure for the request
            jsonObject.put("prompt", cueImagePrompt + titleAndText[1]);  // Use cueText as the prompt
            jsonObject.put("model", "flux");   // Add model
            jsonObject.put("width", 1024);     // Add width
            jsonObject.put("height", 1024);    // Add height
            jsonObject.put("nologo", true);    // Add nologo flag
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(IMAGE_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ApiCallback", "Failed to get image response", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handleImageResponse(response, titleAndText, audioData);
            }
        });
    }

    // Handle the image API response
    private void handleImageResponse(Response response, String[] titleAndText, byte[] audioData) throws IOException {
        if (response.body() != null) {
            byte[] imageData = response.body().bytes(); // Get image data

            // Directly call onSuccess with the title, story text, audio, and image data
            callback.onSuccess(titleAndText, audioData, imageData);
        } else {
            callback.onError(new IOException("Empty response body from image API"));
        }
    }

    // Separate method to make the retention API call
    public void requestRetention() {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("userid", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onError(e);
            return;
        }

        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url(RETENTION_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ApiCallback", "Failed to send retention data", e);
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("ApiCallback", "Retention data successfully sent");
                    callback.onSuccess();
                } else {
                    callback.onError(new IOException("Failed to send retention data"));
                }
            }
        });
    }

    public interface ApiResponseCallback {
        void onSuccess(String[] titleAndText, byte[] audioData, byte[] image);
        void onSuccess();
        void onError(Exception e);
    }

    private static String[] splitText(String input) {
        // Find indices for "Question:" and "Solution:"
        int questionIndex = input.indexOf("Question:");
        int solutionIndex = input.indexOf("Solution:");

        if (questionIndex == -1 || solutionIndex == -1 || solutionIndex < questionIndex) {
            Log.e("CueData", "Invalid input format. Missing 'Question:' or 'Solution:'.");
            return new String[]{"", "", "", "", ""};
        }

        // Extract title (before the first newline)
        String title = input.substring(0, input.indexOf("\n")).trim();

        // Extract story text (everything before "Question:")
        String storyText = input.substring(input.indexOf("\n") + 1, questionIndex).trim();

        // Extract the question and answers (between "Question:" and "Solution:")
        String questionBlock = input.substring(questionIndex + "Question:".length(), solutionIndex).trim();

        // Extract solution (after "Solution:")
        String solution = input.substring(solutionIndex + "Solution:".length()).trim();

        // Split the question and answers into separate lines
        String[] questionParts = questionBlock.split("\n", 2);
        String question = questionParts[0].trim();

        String answers = questionParts.length > 1 ? questionParts[1].trim() : "";

        Log.d("CueData", "Title: " + title);
        Log.d("CueData", "Story Text: " + storyText);
        Log.d("CueData", "Question: " + question);
        Log.d("CueData", "Answers: " + answers);
        Log.d("CueData", "Solution: " + solution);

        return new String[]{title, storyText, question, answers, solution};
    }

}