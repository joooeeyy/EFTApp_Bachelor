package util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eftapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuestionsApi {
    private static final String API_URL = "https://chagpteft.onrender.com/api";
    private static final String SQL_URL = "https://chagpteft.onrender.com/add-user";
    private final String prompText;
    private OkHttpClient client;
    private QuestionsApi.ApiResponseCallback callback;

    public QuestionsApi(Context context, ApiResponseCallback callback) {
        prompText = context.getApplicationContext().getString(R.string.tsrq_poll_questions);
        this.callback = callback;

        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }


    public void getAiQuestions(String goalInput, int userId) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();


        //Questions with longterm goal
        String prompt = prompText + goalInput;

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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ApiCallback", "Failed to get questions", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    String text = jsonResponse.getString("text");
                    Log.d("ApiCallback", "Received questions: " + text);


                    sendAddUserIdRequest(userId, new ApiResponseCallback() {
                        @Override
                        public void onSuccess(String questions) {
                            callback.onSuccess(text);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("AddUserId", "Failed to add user_id", e);
                            callback.onError(e); // Notify the main callback of the failure
                        }
                    });
                    callback.onSuccess(text);

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    callback.onError(e);
                }
            }
        });

    }

    // Function to send the "add user" request
    private void sendAddUserIdRequest(Integer userId, ApiResponseCallback userCallback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            userCallback.onError(e);
            return;
        }

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(SQL_URL) // Adjust to your backend endpoint
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                userCallback.onError(new Exception("Failed")); // Notify the caller of the failure
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("AddUserId", responseBody);
                    userCallback.onSuccess("Success");
                } else {
                    userCallback.onError(new Exception("Failed"));
                }
            }
        });
    }

    public interface ApiResponseCallback {
        void onSuccess(String questions);
        void onError(Exception e);
    }
}
