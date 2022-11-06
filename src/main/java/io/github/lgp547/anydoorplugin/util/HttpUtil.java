package io.github.lgp547.anydoorplugin.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpUtil {
    /**
     * POST请求
     * 参数是：URL，jsonObject(请求参数封装成json对象)
     */
    public static String post(String url, String body) {
        //创建HttpClients对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建post请求对象
        HttpPost httpPost = new HttpPost(url);
        //创建封装请求参数对象，设置post请求参数
        httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        try {
            //执行POST请求，获取请求结果
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                // 发送成功
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                // 发送失败
                return null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
