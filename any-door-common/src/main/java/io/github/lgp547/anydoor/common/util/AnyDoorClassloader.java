package io.github.lgp547.anydoor.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class AnyDoorClassloader extends URLClassLoader {

    public AnyDoorClassloader(List<String> urls) throws Exception {
        super(getUrls(urls), ClassLoader.getSystemClassLoader().getParent());
    }

    public static URL[] getUrls(List<String> filePaths) throws MalformedURLException {
        URL[] urls = new URL[filePaths.size()];
        for (int i = 0; i < filePaths.size(); i++) {
            URL url = new File(filePaths.get(i)).toURI().toURL();
            urls[i] = url;
        }
        return urls;
    }


    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name == null || name.isEmpty()) {
            return null;
        }

        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // 优先处理jdk自带的类，避免抛出ClassNotFoundException
        if (name.startsWith("sun.") || name.startsWith("java.")) {
            return super.loadClass(name, resolve);
        }
        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            // ignore
        }
        return super.loadClass(name, resolve);
    }
}
