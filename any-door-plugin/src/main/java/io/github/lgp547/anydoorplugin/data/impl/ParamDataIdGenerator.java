package io.github.lgp547.anydoorplugin.data.impl;

import io.github.lgp547.anydoorplugin.data.IdGenerator;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-12 13:01
 **/
public class ParamDataIdGenerator implements IdGenerator {
    public static final ParamDataIdGenerator INSTANCE = new ParamDataIdGenerator();

    private AnyDoorSettingsState state;

    private final Object lock;

    private ParamDataIdGenerator() {
        lock = new Object();
    }

    public ParamDataIdGenerator init(AnyDoorSettingsState state) {
        if (this.state != null) {
            throw new IllegalStateException("ParamDataIdGenerator already initialized");
        }
        synchronized (lock) {
            if (this.state == null) {
                this.state = state;
            }
        }

        return this;
    }

    @Override
    public Long nextId() {
        if (state == null) {
            throw new IllegalStateException("ParamDataIdGenerator not initialized");
        }

        synchronized (lock) {
            return ++state.nextId;
        }
    }
}
