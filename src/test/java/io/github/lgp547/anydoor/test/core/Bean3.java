package io.github.lgp547.anydoor.test.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 代理Bean
 */
@Service
public class Bean3 {

    @Autowired
    private Bean bean;

    private void privateMethod() {
        Assert.notNull(bean);
    }

    public void publicMethod() {
        Assert.notNull(bean);
    }
}
