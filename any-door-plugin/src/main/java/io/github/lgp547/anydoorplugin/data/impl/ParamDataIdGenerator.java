package io.github.lgp547.anydoorplugin.data.impl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.IdGenerator;
import io.github.lgp547.anydoorplugin.dialog.DataContext;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-12 13:01
 **/
public class ParamDataIdGenerator implements IdGenerator {
    public static final ParamDataIdGenerator INSTANCE = new ParamDataIdGenerator();
    private final Object lock;

    private volatile Project project;

    private volatile AtomicLong nextId;

    private ParamDataIdGenerator() {
        lock = new Object();
    }

    public ParamDataIdGenerator init(Project project) {
        if (this.project != null) {
            throw new IllegalStateException("ParamDataIdGenerator already initialized");
        }
        synchronized (lock) {
            if (this.project == null) {
                this.project = project;
            }
        }

        return this;
    }

    private void initId() {
        Long id = DataContext.instance(project).currentId();
        nextId = new AtomicLong(Objects.requireNonNullElse(id, 0L));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long nextId() {
        if (project == null) {
            throw new IllegalStateException("ParamDataIdGenerator not initialized");
        }

        if (nextId == null) {
            synchronized (lock) {
                if (nextId == null) {
                    initId();
                }
            }
        }

        return nextId.incrementAndGet();
    }
}
