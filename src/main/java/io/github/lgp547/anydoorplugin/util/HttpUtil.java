package io.github.lgp547.anydoorplugin.util;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public class HttpUtil {

    public static OkHttpClient okHttpClient = new OkHttpClient();

    public static MediaType json = MediaType.parse("application/json; charset=utf-8");

    public static void postAsy(String url, String reqBody, Consumer<Exception> errHandle) {
        Request request = new Request.Builder().url(url).post(RequestBody.create(reqBody, json)).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                errHandle.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            }
        });
    }
}
