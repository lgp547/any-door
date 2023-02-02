package io.github.lgp547.anydoor.controller;

import io.github.lgp547.anydoor.core.AnyDoorService;
import io.github.lgp547.anydoor.dto.AnyDoorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AnyDoorController {

    private static final Logger log = LoggerFactory.getLogger(AnyDoorController.class);

    /**
     * @return 执行方法结果, 异步的话直接null
     */
    @RequestMapping("/any_door/run")
    @ResponseBody
    public Object run(@RequestBody AnyDoorDto anyDoorDto) {
        if (log.isDebugEnabled()) {
            log.debug("any_door run requestBody {}", anyDoorDto);
        }

        AnyDoorService anyDoorService = new AnyDoorService();
        return anyDoorService.run(anyDoorDto);
    }

}
