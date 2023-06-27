package io.github.lgp547.anydoor.mvc;

import io.github.lgp547.anydoor.common.dto.AnyDoorRunDto;
import io.github.lgp547.anydoor.core.AnyDoorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AnyDoorController {

    /**
     * @return 执行方法结果, 异步的话直接null
     */
    @RequestMapping("/any_door/run")
    @ResponseBody
    public Object run(@RequestBody AnyDoorRunDto anyDoorDto) {
        AnyDoorService anyDoorService = new AnyDoorService();
        return anyDoorService.run(anyDoorDto);
    }

}
