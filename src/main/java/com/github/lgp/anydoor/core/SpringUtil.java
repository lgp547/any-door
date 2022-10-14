package com.github.lgp.anydoor.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpringUtil implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SpringUtil.class);

    public static ObjectMapper objectMapper = new ObjectMapper();

    private static ApplicationContext applicationContext;

    private static List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();

    public static Object readObject(Class<?> targetType, @Nullable Class<?> contextClass, String value, MethodParameter parameter) {
        HttpInputMessage httpInputMessage = getHttpInputMessage(value);
        for (HttpMessageConverter<?> converter : httpMessageConverters) {
            GenericHttpMessageConverter<?> genericConverter;
            if (converter instanceof GenericHttpMessageConverter && (genericConverter = ((GenericHttpMessageConverter<?>) converter)).canRead(targetType, contextClass, MediaType.APPLICATION_JSON)) {
                try {
                    return genericConverter.read(targetType, contextClass, httpInputMessage);
                } catch (Exception e) {
                    log.error("readObject IOException ", e);
                    return null;
                }
            }
        }
        log.error("没有对应的消息转换器支持");
        return null;
    }

    public static HttpInputMessage getHttpInputMessage(String value) {
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

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return Objects.requireNonNull(applicationContext).getBean(requiredType);
    }

    public static JsonNode readTree(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("入参 content 不支持");
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
        Map<String, HttpMessageConverter> beansOfType = applicationContext.getBeansOfType(HttpMessageConverter.class);
        SpringUtil.httpMessageConverters = new ArrayList(beansOfType.values());
        log.info("mmmmmmmmmmmmmmmmmmm any-door springUtil init end mmmmmmmmmmmmmmmmmmm");
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        SpringUtil.objectMapper = objectMapper;
    }
}
