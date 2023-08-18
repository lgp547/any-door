package io.github.lgp547.anydoorplugin.data.impl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.IdGenerator;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamIndexData;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-12 13:01
 **/
public class ParamDataIdGenerator implements IdGenerator {

    private final AtomicLong nextId;


    public ParamDataIdGenerator(Project project) {
        ParamIndexService service = project.getService(ParamIndexService.class);
        Data<ParamIndexData> paramIndexDataData = service.find(project.getName());
        Long id = paramIndexDataData.getDataList().stream().map(ParamIndexData::getId).max(Long::compare).orElse(0L);
        nextId = new AtomicLong(id);
    }

    @Override
    public Long nextId() {
        return nextId.incrementAndGet();
    }
}
