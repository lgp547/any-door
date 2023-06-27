package io.github.lgp547.anydoor.mvc;

import io.github.lgp547.anydoor.common.util.AnyDoorSpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 报错处理
 * 请求参数的处理
 */
@Configuration
public class AnyDoorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AnyDoorController.class)
    public AnyDoorController anyController(ApplicationContext[] applicationContexts) {
        AnyDoorSpringUtil.initApplicationContexts(() -> applicationContexts);
        return new AnyDoorController();
    }

}