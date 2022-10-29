package io.github.lgp547.anydoor.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.lgp547.anydoor.controller.AnyController;
import io.github.lgp547.anydoor.controller.AnyDoorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class Controller implements ApplicationRunner {

    @Autowired
    AnyController anyController;

    @RequestMapping("/run")
    @Override
    public void run(ApplicationArguments args) {

        Class<?> clazz = Bean.class;
        Class<?> clazz2 = Bean2.class;
        JsonNode jsonNode = Bean.getContent();
        doRun(clazz, jsonNode);
        doRun(clazz2, jsonNode);

        System.out.println("###################结束###################");
    }

    private void doRun(Class<?> clazz, JsonNode jsonNode) {
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("调用方法 " + method.getName());

            AnyDoorDto anyDoorDto = new AnyDoorDto();
            anyDoorDto.setClassName(clazz.getName());
            anyDoorDto.setMethodName(method.getName());
            anyDoorDto.setContent(jsonNode.toString());
            List<String> parameterTypes = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
            anyDoorDto.setParameterTypes(parameterTypes);

            Object result = anyController.run(anyDoorDto);
            System.out.println();
        }
    }
}
