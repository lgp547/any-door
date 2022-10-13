package com.github.lgp.anydoor.autoconfig;

import com.github.lgp.anydoor.controller.AnyController;
import com.github.lgp.anydoor.core.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class AnyDoorAutoConfiguration {

    @Bean
    @Lazy(value = false)
    public SpringUtil springUtils() {
        return new SpringUtil();
    }


    @Bean
    @ConditionalOnMissingBean(AnyController.class)
    public AnyController anyController() {
        return new AnyController();
    }

}