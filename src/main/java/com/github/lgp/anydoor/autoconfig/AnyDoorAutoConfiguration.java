package com.github.lgp.anydoor.autoconfig;

import com.github.lgp.anydoor.controller.AnyController;
import com.github.lgp.anydoor.core.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 报错处理
 * 请求参数的处理
 */
@Configuration
public class AnyDoorAutoConfiguration {

    @Bean
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