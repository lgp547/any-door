package io.github.lgp547.anydoorplugin.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class HttpUtil {

    public static void postAsyncByJdk(String urlStr, String reqBody, BiConsumer<String, Exception> errHandle) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setUseCaches(false);
                OutputStream os = con.getOutputStream();
                os.write(reqBody.getBytes());
                con.getResponseCode();
            } catch (Exception e) {
                errHandle.accept(urlStr, e);
            }
        });
    }
}
