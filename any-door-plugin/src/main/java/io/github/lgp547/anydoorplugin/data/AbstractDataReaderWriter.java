package io.github.lgp547.anydoorplugin.data;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.openapi.diagnostic.Logger;
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
public abstract class AbstractDataReaderWriter<T extends DataItem> {

    protected static final Logger LOG = Logger.getInstance(AbstractDataReaderWriter.class);

    protected final String DATA_BASE_DIR = "/.idea";
    protected final Map<String, CacheData<T>> dataCache = new ConcurrentHashMap<>();
    protected final Map<String, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();
    protected final Map<String, Future<?>> futureMap = new ConcurrentHashMap<>();

    protected final ExecutorService service = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("data-reader-writer-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());


    protected Data<T> doRead(String key, String filePath) {
        return doReadFileSync(key, filePath);
    }

    protected void forceWrite(String key, String filePath, Data<T> data) {
        doWriteFileAsync(key, filePath, data);
    }

    protected Future<?> doWrite(String key, String filePath, Data<T> data) {
        CacheData<T> cacheData = new CacheData<>(key, data);

        CacheData<T> compute = dataCache.compute(key, (k, v) -> {
            if (Objects.isNull(v) || v.data().getTimestamp() < data.getTimestamp()) {
                Future<?> future = futureMap.get(k);
                if (Objects.nonNull(future) && !future.isDone()) {
                    future.cancel(true);
                }
                return cacheData;
            }
            return v;
        });
        if (Objects.equals(compute, cacheData)) {
            return doWriteFileAsync(key, filePath, data);
        }
        return null;
    }


    protected Data<T> doReadFileSync(String key, String filePath) {
        try {
            return new Reader(key, filePath).call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Future<Data<T>> doReadFileAsync(String key, String filePath) {
        return service.submit(new Reader(key, filePath));
    }

    protected Future<?> doWriteFileAsync(String key, String filePath, Data<T> data) {

        return service.submit(new Writer(key, filePath, data));
    }

    protected CacheData<T> readAndCache(String key, String filePath) {

        CacheData<T> cacheData = dataCache.get(key);
        if (Objects.nonNull(cacheData)) {
            return cacheData;
        }

        return dataCache.computeIfAbsent(key, k -> new CacheData<>(key, doReadFileSync(key, filePath)));
    }

    class Reader implements Callable<Data<T>> {

        private final String key;
        private final String filePath;

        public Reader(String key, String filePath) {
            this.key = key;
            this.filePath = filePath;
        }

        @Override
        public Data<T> call() throws Exception {
            LOG.info(String.format("read data. key [%s] filePath [%s]", key, filePath));

            ReentrantReadWriteLock.ReadLock readLock = lockMap.computeIfAbsent(key, k -> new ReentrantReadWriteLock()).readLock();
            try {
                boolean lock = readLock.tryLock(15, TimeUnit.SECONDS);
                if (lock) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        String fileValue = FileUtil.loadFile(file);
                        return JsonUtil.toJavaBean(fileValue, Data.class);
                    }
                    return new Data<>(key);
                }
            } catch (Exception e) {
                LOG.error(String.format("data read fail. key [%s] filePath [%s]", key, filePath), e);
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
                LOG.info(String.format("read data finish. key [%s] filePath [%s] unlock", key, filePath));
            }
            return null;
        }
    }

    class Writer implements Runnable {

        private String key;
        private final String filePath;
        private final Data<T> data;

        public Writer(String filePath, Data<T> data) {
            this.filePath = filePath;
            this.data = data;
        }

        public Writer(String key, String filePath, Data<T> data) {
            this(filePath, data);
            this.key = key;
        }

        @Override
        public void run() {
            LOG.info(String.format("write data. key [%s] filePath [%s]", key, filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

            String newFile = filePath + "." + data.getTimestamp();
            File file = new File(newFile);

            LOG.info(String.format("write data. key [%s] actualPath [%s] ", key, newFile));

            ReentrantReadWriteLock.WriteLock writeLock = lockMap.computeIfAbsent(data.getIdentity(), k -> new ReentrantReadWriteLock()).writeLock();
            try {

                FileUtil.writeToFile(file, JsonUtil.toStr(data));

                boolean lock = writeLock.tryLock(15, TimeUnit.SECONDS);
                if (lock) {
                    LOG.info("write data override lock");

                    CacheData<T> cacheData = dataCache.get(key);
                    if (Objects.isNull(cacheData) || Objects.isNull(cacheData.data()) || cacheData.data().getTimestamp() <= data.getTimestamp()) {
                        LOG.info(String.format("write data override file. key [%s] src [%s] dst [%s] ", key, newFile, filePath));
                        FileUtil.rename(file, fileName);
                    }
                }
            } catch (Exception e) {
                LOG.error(String.format("data write fail. key [%s] filePath [%s]", key, filePath), e);
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();

                File writeFile = new File(newFile);
                if (writeFile.exists()) {
                    FileUtil.delete(writeFile);
                }

                LOG.info(String.format("write data finish. key [%s] filePath [%s] unlock", key, filePath));
            }
        }
    }

}
