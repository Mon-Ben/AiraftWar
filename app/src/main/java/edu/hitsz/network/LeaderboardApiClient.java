package edu.hitsz.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LeaderboardApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    public interface UploadCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface FetchCallback {
        void onSuccess(List<LeaderboardEntry> entries);
        void onError(String message);
    }

    public void uploadScore(LeaderboardEntry entry, UploadCallback callback) {
        RequestBody body = RequestBody.create(gson.toJson(entry), JSON);
        Request request = new Request.Builder()
                .url(NetworkConfig.SCORES_URL)
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("HTTP " + response.code());
                }
                response.close();
            }
        });
    }

    public void fetchScores(FetchCallback callback) {
        Request request = new Request.Builder()
                .url(NetworkConfig.SCORES_URL)
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("HTTP " + response.code());
                    response.close();
                    return;
                }
                String json = response.body().string();
                response.close();
                try {
                    Type type = new TypeToken<List<LeaderboardEntry>>() { }.getType();
                    List<LeaderboardEntry> entries = gson.fromJson(json, type);
                    if (entries == null) {
                        entries = Collections.emptyList();
                    }
                    entries.sort(Comparator.comparingInt(LeaderboardEntry::getScore).reversed());
                    callback.onSuccess(entries);
                } catch (Exception e) {
                    callback.onError("JSON解析失败: " + e.getMessage());
                }
            }
        });
    }
}
