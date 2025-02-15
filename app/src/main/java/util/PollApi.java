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

public class PollApi {
    private static final String API_URL = "https://chagpteft.onrender.com/add-poll-result";

    private OkHttpClient client;
    private PollApiCallback callback;

    public PollApi(PollApiCallback callback) {
        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void submitPoll(ArrayList<Double> results, int userId) {
        double d1,d2,d3,d4,d5;

        d1 = results.get(0);
        d2 = results.get(1);
        d3 = results.get(2);
        d4 = results.get(3);
        d5 = results.get(4);

        Log.d("PollApii", "d1: " + d1);


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid", userId);
            jsonObject.put("poll_result_dd", d1);
            jsonObject.put("poll_result_ba", d2);
            jsonObject.put("poll_tsrq_value1", d3);
            jsonObject.put("poll_tsrq_value2", d4);
            jsonObject.put("poll_tsrq_value3", d5);
        } catch (JSONException e) {
            e.printStackTrace();
            //userCallback.onError(e);
            return;
        }

        Log.d("PollApii", "Request Body: " + jsonObject.toString());

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(API_URL) // Adjust to your backend endpoint
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PollApi", "Failed to get text response", e);
                callback.onError(new Exception("PollApi Error"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                callback.onSuccess();
            }
        });
    }

    public interface PollApiCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
