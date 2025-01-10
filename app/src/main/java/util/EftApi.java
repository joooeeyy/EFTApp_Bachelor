package util;

import android.util.Log;

import androidx.annotation.NonNull;

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
    private final String cueTextPrompt = "I want you to write an immersive and vivid story with just a few parameters, based on episodic future thinking. The story must serve as a cue to help simulate the future. The parameters are Where, What, Who, When, How, and Subject. I want you the write the first line" +
            "as a title than a new line for the cue text. Get ready!\n";
    private final String cueImagePrompt = "Make a simplistic enviroment picture as motivation without using any humans or animals, which fits the following text: ";

    private OkHttpClient client;
    private ApiResponseCallback callback;

    public EftApi(ApiResponseCallback callback) {
        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    // First API call to get text response
    public void aiTextResponse(ArrayList<String> inputs) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();

        //where when what how who subject
        String prompt = cueTextPrompt + "\n where: " + inputs.get(0) + "\n when: " + inputs.get(1) +
                "\n what: " + inputs.get(2) + "\n how: " + inputs.get(3) + "\n who: " + inputs.get(4) +
                "\n subject" + inputs.get(5);

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
                    Log.d("ApiCallback", "Received text: " + text);

                    // Once we get the text, make the second API call to get the audio file
                    requestTextToSpeech(text);

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Second API call to convert the text to speech (audio)
    private void requestTextToSpeech(String input) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();

        String[] titleAndText = splitText(input);

        try {
            jsonObject.put("text", titleAndText[1]);
            jsonObject.put("languageCode", "en-US");
            jsonObject.put("gender", "FEMALE");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(TTS_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
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
                try {
                    if (response.body() != null) {
                        byte[] imageData = response.body().bytes();
                        // Pass the response back to the callback (ViewModel or Repository)
                        callback.onSuccess(titleAndText, audioData, imageData);
                    }
                } catch (IOException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public interface ApiResponseCallback {
        void onSuccess(String[] titleAndText, byte[] audioData, byte[] image);
        void onError(Exception e);
    }

    private String[] splitText(String input) {
        String title = input.contains("\n") ? input.substring(0, input.indexOf("\n")) : input;
        String text = input.contains("\n") ? input.substring(input.indexOf("\n") + 1) : "";

        return new String[] {title, text};
    }
}

