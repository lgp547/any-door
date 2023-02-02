package io.github.lgp547.anydoor.test.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.lgp547.anydoor.controller.AnyDoorController;
import io.github.lgp547.anydoor.dto.AnyDoorDto;
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
    AnyDoorController anyDoorController;

    @RequestMapping("/run")
    @Override
    public void run(ApplicationArguments args) {

        Class<?> clazz = Bean.class;
        Class<?> clazz2 = Bean2.class;
        Class<?> clazz3 = Bean3.class;
        JsonNode jsonNode = Bean.getContent();
        doRun(clazz, jsonNode);
        doRun(clazz2, jsonNode);
        doRun(clazz3, jsonNode);

        System.out.println("###################结束###################");
    }

    private void doRun(Class<?> clazz, JsonNode jsonNode) {
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("调用方法 " + clazz.getSimpleName() + " " + method.getName());

            AnyDoorDto anyDoorDto = new AnyDoorDto();
            anyDoorDto.setClassName(clazz.getName());
            anyDoorDto.setMethodName(method.getName());
            anyDoorDto.setContent(jsonNode.toString());
            List<String> parameterTypes = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
            anyDoorDto.setParameterTypes(parameterTypes);
            anyDoorDto.setSync(true);

            try {
                Object result = anyDoorController.run(anyDoorDto);
            } catch (Exception e) {
                if (method.getName().equals("exception")) {
                    continue;
                } else {
                    throw new RuntimeException(e);
                }
            }
            System.out.println();
        }
    }
}
