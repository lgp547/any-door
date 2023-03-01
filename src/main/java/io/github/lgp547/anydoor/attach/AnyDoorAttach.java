package io.github.lgp547.anydoor.attach;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

import io.github.lgp547.anydoor.core.AnyDoorService;
import io.github.lgp547.anydoor.dto.AnyDoorDto;
import io.github.lgp547.anydoor.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnyDoorAttach {

    private static final Logger log = LoggerFactory.getLogger(AnyDoorAttach.class);

    public static void agentmain(String agentArgs, Instrumentation inst) {
        if (log.isDebugEnabled()) {
            log.debug("any_door agentmain requestBody {}", agentArgs);
        }

        if (agentArgs != null && agentArgs.startsWith("file://")) {
            try {
                agentArgs = getTextFileAsString(new File(agentArgs.substring(7)));
            } catch (IOException e) {
                log.error("read any door param file error {}", e.getMessage());
                throw new IllegalArgumentException("read any door param file error" + e.getMessage());
            }
        }

        AnyDoorDto anyDoorDto = JsonUtil.toJavaBean(agentArgs, AnyDoorDto.class);

        AnyDoorService anyDoorService = new AnyDoorService();
        anyDoorService.run(anyDoorDto);
    }

    public static String getTextFileAsString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            return sb.toString();
        }
    }
}
