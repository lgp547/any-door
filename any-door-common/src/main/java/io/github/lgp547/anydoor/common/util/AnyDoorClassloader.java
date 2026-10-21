/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.github.lgp547.anydoor.common.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class AnyDoorClassloader extends URLClassLoader {
    
    private boolean end = false;

    /** 目标类加载器，用于共享 Kotlin 元数据和默认构造器标记的 Class 身份。 */
    private final ClassLoader targetClassLoader;
    
    public AnyDoorClassloader(List<String> urls) throws Exception {
        this(urls, null);
    }

    /**
     * 创建隔离加载器，并让 Kotlin 元数据和默认构造器标记与目标类共享 Class 身份。
     *
     * @param urls 隔离运行端所需的 jar 路径
     * @param targetClassLoader 目标类的加载器
     * @throws Exception URLClassLoader 初始化失败时抛出
     */
    public AnyDoorClassloader(List<String> urls, ClassLoader targetClassLoader) throws Exception {
        super(getUrls(urls), ClassLoader.getSystemClassLoader().getParent());
        this.targetClassLoader = targetClassLoader;
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

        // Kotlin 元数据注解与默认构造器标记需和目标类共享 Class 身份。
        if (targetClassLoader != null && ("kotlin.Metadata".equals(name)
                || "kotlin.jvm.internal.DefaultConstructorMarker".equals(name))) {
            try {
                return targetClassLoader.loadClass(name);
            } catch (ClassNotFoundException ignored) {
                // 目标加载器缺少该 Kotlin 类时，继续从隔离运行端的 URL 中查找。
            }
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
    
    /**
     * 由于存在异步执行，若事先关闭了会导致异步里面涉及当前加载器的类加载失败
     */
    @Override
    public void close() throws IOException {
        if (end) {
            super.close();
        }
    }
    
    public void forceClose() {
        end = true;
        try {
            super.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setEnd(boolean end) {
        this.end = end;
    }
    
}
