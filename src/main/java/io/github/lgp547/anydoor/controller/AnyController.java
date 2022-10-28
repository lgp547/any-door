package io.github.lgp547.anydoor.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.lgp547.anydoor.core.AnyDoorHandlerMethod;
import io.github.lgp547.anydoor.core.ClassUtil;
import io.github.lgp547.anydoor.core.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 支持null参数
 * 支持私有方法
 * 修复String类型
 * 修复List类型的泛型检查
 * 支持静态方法
 */
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
        Object bean = SpringUtil.getBean(clazz);
        Method method = ClassUtil.getMethod(AopUtils.getTargetClass(bean), methodName, parameterTypes);

        AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(bean, method);
        return handlerMethod.invoke(jsonNode);
    }

}
