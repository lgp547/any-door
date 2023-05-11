package io.github.lgp547.anydoor.util.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.github.lgp547.anydoor.util.LambdaUtil;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AnyDoorTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer(DATETIME_FORMAT);


    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        LocalDateTime localDateTime;
        String text = jsonParser.getText();

        localDateTime = LambdaUtil.runNotExc(() -> LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text)), ZoneId.systemDefault()));
        if (null == localDateTime) {
            localDateTime = LambdaUtil.runNotExc(() -> getLocalDateTime(jsonParser, deserializationContext, INSTANCE));
        }
        if (null == localDateTime) {
            localDateTime = LambdaUtil.runNotExc(() -> getLocalDateTime(jsonParser, deserializationContext, LocalDateTimeDeserializer.INSTANCE));
        }
        return localDateTime;
    }

    private LocalDateTime getLocalDateTime(JsonParser jsonParser, DeserializationContext deserializationContext, LocalDateTimeDeserializer instance) {
        try {
            return instance.deserialize(jsonParser, deserializationContext);
        } catch (IOException e) {
            return null;
        }
    }
}
