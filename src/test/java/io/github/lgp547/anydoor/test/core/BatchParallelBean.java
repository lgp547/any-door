package io.github.lgp547.anydoor.test.core;

public class BatchParallelBean {

    private static int num = 0;

    public Integer parallel() {
        return num++;
    }

}
