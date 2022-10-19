package io.github.lgp.anydoor;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.lgp.anydoor.core.AnyDoorHandlerMethod;
import io.github.lgp.anydoor.core.SpringUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@RestController
public class Test {

    @RequestMapping("/test")
    public void test() {
        Class<?> clazz = TestRun.class;
        JsonNode jsonNode = SpringUtil.readTree(TestRun.content);
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("调用方法 " + method.getName());

            AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(SpringUtil.getBean(clazz), method);
            Object result = handlerMethod.invoke(jsonNode);

            System.out.println("调用方法的返回结果 " + result);
            System.out.println();
        }
    }
}
