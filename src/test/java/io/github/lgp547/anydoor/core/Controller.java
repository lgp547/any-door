package io.github.lgp547.anydoor.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@RestController
public class Controller implements ApplicationRunner {

    @RequestMapping("/run")
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Class<?> clazz = Bean.class;
        JsonNode jsonNode = Bean.getContent();
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("调用方法 " + method.getName());

            AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(new Bean(), method);
            Object result = handlerMethod.invoke(jsonNode);

            System.out.println("调用方法 " + method.getName() + " result:" + result);
            System.out.println();
        }
    }
}
