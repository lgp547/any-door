package io.github.lgp547.anydoor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringWebmvcUtil {

    private static final Logger log = LoggerFactory.getLogger(SpringWebmvcUtil.class);

    private static List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();

    public static boolean webmvcSupport = false;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void init(ApplicationContext applicationContext) {
        try {
            Map<String, HttpMessageConverter> beansOfType = applicationContext.getBeansOfType(HttpMessageConverter.class);
            SpringWebmvcUtil.httpMessageConverters = new ArrayList(beansOfType.values());
        } catch (Exception e) {
            log.debug("SpringWebmvcUtil init fail", e);
        }
        webmvcSupport = true;
    }

    private static HttpInputMessage getHttpInputMessage(String value) {
        return new HttpInputMessage() {

            @Override
            public HttpHeaders getHeaders() {
                return HttpHeaders.EMPTY;
            }

            @Override
            public InputStream getBody() throws IOException {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
                outputStreamWriter.write(value.toCharArray());
                outputStreamWriter.close();

                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            }
        };
    }

    @Nullable
    public static Object readObject(Type targetType, @Nullable Class<?> contextClass, String value) {
        if (!webmvcSupport) {
            return null;
        }
        if (!StringUtils.hasText(value)) {
            return null;
        }
        HttpInputMessage httpInputMessage = SpringWebmvcUtil.getHttpInputMessage(value);
        for (HttpMessageConverter<?> converter : httpMessageConverters) {
            GenericHttpMessageConverter<?> genericConverter;
            if (converter instanceof GenericHttpMessageConverter &&
                    (genericConverter = ((GenericHttpMessageConverter<?>) converter)).canRead(targetType, contextClass, MediaType.APPLICATION_JSON)) {
                try {
                    return genericConverter.read(targetType, contextClass, httpInputMessage);
                } catch (Exception e) {
                    log.error("SpringWebmvcUtil readObject IOException {}", e.getMessage());
                    return null;
                }
            }
        }
        log.error("SpringWebmvcUtil 没有对应的消息转换器支持");
        return null;
    }
}
