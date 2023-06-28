package io.github.lgp547.anydoor.test.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.lgp547.anydoor.common.dto.AnyDoorRunDto;
import io.github.lgp547.anydoor.core.AnyDoorService;
import io.github.lgp547.anydoor.util.JsonUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class Controller implements ApplicationRunner {

    @RequestMapping("/run")
    @Override
    public void run(ApplicationArguments args) {

        Class<?> clazz = Bean.class;
        Class<?> clazz2 = DtoBean.class;
        Class<?> clazz3 = AopBean.class;
        JsonNode jsonNode = Bean.getContent();
        doRun(clazz, jsonNode);
        doRun(clazz2, jsonNode);
        doRun(clazz3, jsonNode);
        doRun(BatchParallelBean.class, jsonNode);
        doRun(BatchConcurrentBean.class, jsonNode);

        System.out.println("###################成功结束###################");
    }

    private void doRun(Class<?> clazz, JsonNode jsonNode) {
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("调用方法 " + clazz.getSimpleName() + " " + method.getName());

            AnyDoorRunDto anyDoorDto = new AnyDoorRunDto();
            anyDoorDto.setClassName(clazz.getName());
            anyDoorDto.setMethodName(method.getName());
            String content = JsonUtil.toStrNotExc(jsonNode);
            anyDoorDto.setContent(Objects.requireNonNull(content));
            List<String> parameterTypes = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
            anyDoorDto.setParameterTypes(parameterTypes);
            anyDoorDto.setSync(true);
            if (Objects.equals(clazz, BatchParallelBean.class)) {
                anyDoorDto.setNum(10);
                anyDoorDto.setConcurrent(false);
            }
            if (Objects.equals(clazz, BatchConcurrentBean.class)) {
                anyDoorDto.setSync(false);
                anyDoorDto.setNum(10);
                anyDoorDto.setConcurrent(true);
            }

            AnyDoorService anyDoorService = new AnyDoorService();
            try {
                anyDoorService.run(anyDoorDto);
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
