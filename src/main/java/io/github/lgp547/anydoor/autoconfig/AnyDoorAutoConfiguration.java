package io.github.lgp547.anydoor.autoconfig;

import io.github.lgp547.anydoor.controller.AnyController;
import io.github.lgp547.anydoor.core.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 报错处理
 * 请求参数的处理
 */
@Configuration
public class AnyDoorAutoConfiguration {

    @Bean
    @Lazy(value = false)
    @ConditionalOnMissingBean(SpringUtil.class)
    public SpringUtil springUtil() {
        return new SpringUtil();
    }


    @Bean
    @ConditionalOnMissingBean(AnyController.class)
    public AnyController anyController() {
        return new AnyController();
    }

}