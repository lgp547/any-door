package io.github.lgp547.anydoorplugin.data.impl;

import java.util.UUID;

import io.github.lgp547.anydoorplugin.data.IdGenerator;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-12 20:04
 **/
public class DefaultIdGenerator implements IdGenerator {
    @Override
    public Long nextId() {
        return UUID.randomUUID().getLeastSignificantBits();
    }
}
