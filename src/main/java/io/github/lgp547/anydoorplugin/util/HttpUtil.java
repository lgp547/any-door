package io.github.lgp547.anydoorplugin.util;

import cn.hutool.core.thread.ThreadUtil;

import java.util.function.Consumer;

public class HttpUtil {

//    public static OkHttpClient okHttpClient = new OkHttpClient();
//
//    public static MediaType JSON = MediaType.get("application/json; charset=utf-8");
//
//    public static void postAsy(String url, String reqBody, Consumer<Exception> errHandle) {
//        Request request = new Request.Builder().url(url).post(RequestBody.create(reqBody, JSON)).build();
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                errHandle.accept(e);
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//            }
//        });
//    }

    public static void postAsy(String url, String reqBody, Consumer<Exception> errHandle) {
        ThreadUtil.execAsync(() -> {
            try {
                cn.hutool.http.HttpUtil.post(url, reqBody);
            } catch (Exception e) {
                errHandle.accept(e);
            }
        });
    }
}
