package io.github.lgp547.anydoorplugin.data;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.intellij.openapi.util.io.FileUtil;
import io.github.lgp547.anydoorplugin.data.domain.CacheData;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.DataItem;
import io.github.lgp547.anydoorplugin.util.JsonUtil;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-29 11:11
 **/
public abstract class DataReaderWriter<T extends DataItem> {

    protected final String DATA_BASE_DIR = "/.idea";
    protected final Map<String, CacheData<T>> dataCache = new ConcurrentHashMap<>();
    protected final Map<String, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    protected abstract Data<T> readOneFile(String qualifiedName, String filePath);

    protected Data<T> doReadFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                String fileValue = FileUtil.loadFile(file);
                return JsonUtil.toJavaBean(fileValue, Data.class);
            } catch (IOException e) {
                //TODO
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected boolean doWriteFile(String filePath, Data<T> data) {
        File file = new File(filePath);
        FileUtil.createIfNotExists(file);

        try {
            String fileValue = JsonUtil.toStr(data);
            FileUtil.writeToFile(file, fileValue);
            return true;
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    class Writer implements Runnable {
        private final String filePath;
        private final Data<T> data;

        public Writer(String filePath, Data<T> data) {
            this.filePath = filePath;
            this.data = data;
        }

        @Override
        public void run() {
            File file = new File(filePath);
            try {

                String fileValue = JsonUtil.toStr(data);
                FileUtil.writeToFile(file, fileValue);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
