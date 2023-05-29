package io.github.lgp547.anydoor.util.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;

import java.time.LocalDateTime;

public class AnyDoorJavaTimeModule extends SimpleModule {

    public AnyDoorJavaTimeModule() {
        super(PackageVersion.VERSION);
        this.addDeserializer(LocalDateTime.class, new AnyDoorTimeDeserializer());
    }

}