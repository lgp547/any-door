package io.github.lgp547.anydoorplugin.data.impl;

import java.util.Objects;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.domain.CacheData;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.DataReaderWriter;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-29 11:34
 **/
public class ParamDataReaderWriter extends DataReaderWriter<ParamDataItem> {

    public Data<ParamDataItem> readClassParamData(Project project, String identity) {
        Objects.requireNonNull(identity);

        String key = genKey(project, identity);

        return dataCache.computeIfAbsent(key, k -> readFromFileAndCreateCache(key, project, identity)).data();
    }

    private CacheData<ParamDataItem> readFromFileAndCreateCache(String key, Project project, String identity) {

        CacheData<ParamDataItem> cacheData = dataCache.get(key);
        if (Objects.nonNull(cacheData)) {
            return cacheData;
        }
        return new CacheData<>(key, identity, readOneFile(identity, getFilePath(project.getBasePath(), identity)));

    }

    @Override
    protected Data<ParamDataItem> readOneFile(String qualifiedName, String filePath) {
        Data<ParamDataItem> data = doReadFile(filePath);
        if (Objects.isNull(data)) {
            data = new Data<>(qualifiedName);
            doWriteFile(filePath, data);
        }
        return data;
    }

    private String getFilePath(String projectPath, String qualifiedName) {
        if (!isValidClassName(qualifiedName)) {
            throw new IllegalArgumentException("invalid class name");
        }

        String separator = System.getProperty("file.separator");
        String filepath = qualifiedName.replace(".", separator) + ".json";

        return projectPath + DATA_BASE_DIR + separator + filepath;
    }

    private boolean isValidClassName(String className) {
        return className.matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");
    }


    private static String genKey(Project project, String qualifiedName) {
        return project.getBasePath() + qualifiedName;
    }
}
