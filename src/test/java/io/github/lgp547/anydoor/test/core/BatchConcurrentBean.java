package io.github.lgp547.anydoor.test.core;

import java.util.concurrent.atomic.AtomicInteger;

public class BatchConcurrentBean {

    private static final AtomicInteger num = new AtomicInteger(0);

    public Integer concurrent() {
        return num.incrementAndGet();
    }
}
