package io.github.lgp547.anydoor.attach;

import io.github.lgp547.anydoor.core.AnyDoorService;
import io.github.lgp547.anydoor.dto.AnyDoorDto;
import io.github.lgp547.anydoor.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class AnyDoorAttach {

    private static final Logger log = LoggerFactory.getLogger(AnyDoorAttach.class);

    public static void agentmain(String agentArgs, Instrumentation inst) {
        if (log.isDebugEnabled()) {
            log.debug("any_door agentmain requestBody {}", agentArgs);
        }

        AnyDoorDto anyDoorDto = JsonUtil.toJavaBean(agentArgs, AnyDoorDto.class);

        AnyDoorService anyDoorService = new AnyDoorService();
        anyDoorService.run(anyDoorDto);
    }
}
