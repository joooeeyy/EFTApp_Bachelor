package repository;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import ViewModel.ApiViewModel;
import persistance.Cue;
import persistance.CueDao;
import persistance.CueDatabase;
import util.EftApi;

public class CueApiRepository implements EftApi.ApiResponseCallback {

    private EftApi eftApi;
    private CueDao cueDao;
    private ApiViewModel apiViewModel;

    public CueApiRepository(Context context, ApiViewModel apiViewModel) {
        CueDatabase cueDatabase = CueDatabase.getInstance(context);
        this.cueDao = cueDatabase.cueDao();
        this.eftApi = new EftApi(this);
        this.apiViewModel = apiViewModel;
    }

    public void fetchCueData(ArrayList<String> inputs) {
        eftApi.aiTextResponse(inputs);  // Calls the first API to get the text and then requests the audio
    }

    @Override
    public void onSuccess(String[] titleAndText, byte[] audioData, byte[] image) {
        String title = titleAndText[0];
        String text = titleAndText[1];
        Cue cue = new Cue(title, text, audioData, image);
        cueDao.insert(cue);
        apiViewModel.setApiResponse(true);
    }

    @Override
    public void onError(Exception e) {
        Log.d("ResponseCallbackError", "No Api Response");
        apiViewModel.setApiResponse(false);
    }
}
