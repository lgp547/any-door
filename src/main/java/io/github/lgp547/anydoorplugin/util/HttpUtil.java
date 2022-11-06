package io.github.lgp547.anydoorplugin.util;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public class HttpUtil {

    public static OkHttpClient okHttpClient = new OkHttpClient();

    public static MediaType json = MediaType.parse("application/json; charset=utf-8");

    /**
     * POST请求
     */
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
