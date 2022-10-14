package com.github.lgp.anydoor.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.lgp.anydoor.core.AnyDoorHandlerMethod;
import com.github.lgp.anydoor.core.ClassUtil;
import com.github.lgp.anydoor.core.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.List;

@Controller
public class AnyController {

    private static final Logger log = LoggerFactory.getLogger(AnyController.class);

    /**
     * @return 执行方法结果
     */
    @RequestMapping("/any_door/run")
    @ResponseBody
    public Object run(@RequestBody AnyDoorDto anyDoorDto) throws IllegalArgumentException {
        if (log.isDebugEnabled()) {
            log.debug("any-door run requestBody {}", anyDoorDto);
        }

        anyDoorDto.verify();

        String className = anyDoorDto.getClassName();
        String methodName = anyDoorDto.getMethodName();
        String content = anyDoorDto.getContent();
        List<String> parameterTypes = anyDoorDto.getParameterTypes();

        JsonNode jsonNode = SpringUtil.readTree(content);
        Class<?> clazz = ClassUtil.forName(className);
        Method method = ClassUtil.getMethod(clazz, methodName, parameterTypes);

        AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(SpringUtil.getBean(clazz), method);
        return handlerMethod.invoke(jsonNode);
    }

}
