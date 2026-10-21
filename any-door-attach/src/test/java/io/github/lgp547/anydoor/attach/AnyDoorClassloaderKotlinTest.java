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

package io.github.lgp547.anydoor.attach;

import io.github.lgp547.anydoor.common.util.AnyDoorClassloader;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * 验证 attach 运行端与目标应用隔离加载 Kotlin 类型时共享 Kotlin 类身份。
 *
 * @author luoweiyu
 * @date 2026-10-20
 */
public class AnyDoorClassloaderKotlinTest {

    /** Kotlin fixture 的二进制类名；用字符串避免测试加载器预先加载该类。 */
    private static final String FIXTURE_CLASS = "io.github.lgp547.anydoor.attach.fixture.IsolatedKotlinFixture";

    /** 非 Spring 目标的私有调用保留原实例，避免在 attach 加载器中解析可选的 Spring 类型。 */
    @Test
    public void privateInvocationKeepsInstanceWhenSpringIsUnavailable() throws Exception {
        URL[] runtimeUrls = {
                AnyDoorAttach.class.getProtectionDomain().getCodeSource().getLocation(),
                AnyDoorClassloader.class.getProtectionDomain().getCodeSource().getLocation()
        };
        String stdlibPath = new File(Class.forName("kotlin.Unit").getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();
        URL[] targetUrls = {
                new File("target/test-classes").toURI().toURL(),
                new File(stdlibPath).toURI().toURL()
        };
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        try (URLClassLoader runtimeClassLoader = new URLClassLoader(runtimeUrls, null);
             URLClassLoader targetClassLoader = new URLClassLoader(targetUrls, parent)) {
            assertClassNotFound(runtimeClassLoader, "org.springframework.util.Assert");
            assertClassNotFound(targetClassLoader, "org.springframework.util.Assert");
            Class<?> fixtureClass = targetClassLoader.loadClass(FIXTURE_CLASS);
            Object candidate = fixtureClass.getField("INSTANCE").get(null);
            Class<?> resolverClass = runtimeClassLoader.loadClass(
                    "io.github.lgp547.anydoor.attach.InvocationInstanceResolver");
            Method resolver = resolverClass.getDeclaredMethod("resolve", Object.class, boolean.class);
            resolver.setAccessible(true);
            Object resolved = resolver.invoke(null, candidate, true);
            assertSame(candidate, resolved);

            try {
                resolver.invoke(null, new Object[]{null, true});
                fail("私有调用实例为 null 时应保留既有校验契约");
            } catch (java.lang.reflect.InvocationTargetException expected) {
                assertEquals(IllegalArgumentException.class, expected.getCause().getClass());
                assertEquals("Candidate must not be null", expected.getCause().getMessage());
            }
        }
    }

    /** Spring 目标仍需解开 JDK 与 CGLIB 代理，以便访问实际目标声明的私有方法。 */
    @Test
    public void privateInvocationUnwrapsSpringJdkAndCglibProxies() {
        InvocationTarget target = new InvocationTargetImpl();

        org.springframework.aop.framework.ProxyFactory jdkProxyFactory =
                new org.springframework.aop.framework.ProxyFactory(target);
        jdkProxyFactory.setProxyTargetClass(false);
        Object jdkProxy = jdkProxyFactory.getProxy();
        assertSame(target, InvocationInstanceResolver.resolve(jdkProxy, true));

        org.springframework.aop.framework.ProxyFactory cglibProxyFactory =
                new org.springframework.aop.framework.ProxyFactory(target);
        cglibProxyFactory.setProxyTargetClass(true);
        Object cglibProxy = cglibProxyFactory.getProxy();
        assertSame(target, InvocationInstanceResolver.resolve(cglibProxy, true));
    }

    private interface InvocationTarget {
        String invoke();
    }

    private static class InvocationTargetImpl implements InvocationTarget {
        @Override
        public String invoke() {
            return "target";
        }
    }

    /**
     * 目标应用只有 fixture 与 Kotlin 标准库时，隔离运行端仍能反序列化目标 data class。
     */
    @Test
    public void loadsReflectionSupportFromIsolatedLoaderWhenTargetHasOnlyStdlib() throws Exception {
        List<String> paths = Arrays.stream(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"))
                        .split(java.util.regex.Pattern.quote(File.pathSeparator)))
                .filter(path -> !path.isEmpty())
                .collect(Collectors.toList());
        String stdlibPath = new File(Class.forName("kotlin.Unit").getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();
        List<String> targetPaths = Arrays.asList(new File("target/test-classes").getAbsolutePath(), stdlibPath);
        URL[] targetUrls = targetPaths.stream().map(path -> {
            try {
                return new File(path).toURI().toURL();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }).toArray(URL[]::new);
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        try (URLClassLoader targetClassLoader = new URLClassLoader(targetUrls, parent);
             AnyDoorClassloader anyDoorClassloader = new AnyDoorClassloader(paths, targetClassLoader)) {
            assertClassNotFound(targetClassLoader, "kotlin.reflect.jvm.internal.ReflectionFactoryImpl");
            assertClassNotFound(targetClassLoader, "kotlin.reflect.jvm.internal.KClassImpl");
            assertClassNotFound(targetClassLoader, "com.fasterxml.jackson.module.kotlin.KotlinModule");
            Class<?> targetMetadata = targetClassLoader.loadClass("kotlin.Metadata");
            assertSame(targetMetadata, anyDoorClassloader.loadClass("kotlin.Metadata"));
            assertSame(targetClassLoader.loadClass("kotlin.jvm.internal.DefaultConstructorMarker"),
                    anyDoorClassloader.loadClass("kotlin.jvm.internal.DefaultConstructorMarker"));
            Class<?> reflectionClass = anyDoorClassloader.loadClass("kotlin.jvm.internal.Reflection");
            assertSame(anyDoorClassloader, reflectionClass.getClassLoader());
            Field factoryField = reflectionClass.getDeclaredField("factory");
            factoryField.setAccessible(true);
            assertEquals("kotlin.reflect.jvm.internal.ReflectionFactoryImpl", factoryField.get(null).getClass().getName());
            assertSame(anyDoorClassloader, anyDoorClassloader.loadClass("kotlin.reflect.jvm.internal.ReflectionFactoryImpl").getClassLoader());
            assertSame(anyDoorClassloader, anyDoorClassloader.loadClass("kotlin.reflect.jvm.internal.KClassImpl").getClassLoader());
            Class<?> reflectJvmMapping = anyDoorClassloader.loadClass("kotlin.reflect.jvm.ReflectJvmMapping");
            assertSame(anyDoorClassloader, reflectJvmMapping.getClassLoader());
            Object reflectedFixtureClass = reflectionClass.getMethod("getOrCreateKotlinClass", Class.class)
                    .invoke(null, targetClassLoader.loadClass(FIXTURE_CLASS));
            assertSame(anyDoorClassloader, reflectedFixtureClass.getClass().getClassLoader());
            assertEquals("kotlin.reflect.jvm.internal.KClassImpl", reflectedFixtureClass.getClass().getName());

            Class<?> fixtureClass = targetClassLoader.loadClass(FIXTURE_CLASS);
            Object fixture = fixtureClass.getField("INSTANCE").get(null);
            Class<?> handlerClass = anyDoorClassloader.loadClass("io.github.lgp547.anydoor.core.AnyDoorHandlerMethod");
            Method payloadMethod = fixtureClass.getDeclaredMethod("payload",
                    targetClassLoader.loadClass("io.github.lgp547.anydoor.attach.fixture.IsolatedPayload"));
            Object handler = handlerClass.getConstructor(Object.class, Method.class).newInstance(fixture, payloadMethod);
            Map<String, Object> content = new HashMap<>();
            content.put("args0", java.util.Collections.singletonMap("value", "isolated"));
            Object result = handlerClass.getMethod("invokeSync", Runnable.class, Map.class)
                    .invoke(handler, (Runnable) () -> { }, content);
            assertEquals("isolated!", result);
            anyDoorClassloader.setEnd(true);
        }
    }

    private static void assertClassNotFound(ClassLoader classLoader, String className) throws Exception {
        try {
            classLoader.loadClass(className);
            fail("目标应用不应包含类：" + className);
        } catch (ClassNotFoundException expected) {
            // 目标 classpath 有意只包含 fixture 与 Kotlin 标准库。
        }
    }
}
