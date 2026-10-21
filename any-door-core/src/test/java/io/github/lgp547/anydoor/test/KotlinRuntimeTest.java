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

package io.github.lgp547.anydoor.test;

import io.github.lgp547.anydoor.core.AnyDoorHandlerMethod;
import io.github.lgp547.anydoor.core.AnyDoorService;
import io.github.lgp547.anydoor.common.dto.AnyDoorRunDto;
import io.github.lgp547.anydoor.common.util.AnyDoorBeanUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorAopUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorSpringUtil;
import io.github.lgp547.anydoor.test.fixture.KotlinPrivateMethodFixture;
import io.github.lgp547.anydoor.test.fixture.KotlinObjectFixture;
import io.github.lgp547.anydoor.test.fixture.KotlinScheduleFixture;
import io.github.lgp547.anydoor.test.fixture.KotlinSpringFixture;
import io.github.lgp547.anydoor.test.fixture.KotlinStaticOnly;
import io.github.lgp547.anydoor.test.fixture.KotlinTypeFixture;
import io.github.lgp547.anydoor.util.JsonUtil;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 验证运行端对普通 Kotlin/JVM 方法的最小调用与参数绑定能力。
 *
 * @author luoweiyu
 * @date 2026-10-20
 */
public class KotlinRuntimeTest {

    /**
     * JVM 类型名解析应覆盖原始数组、引用数组及嵌套类二进制名。
     */
    @Test
    public void resolvesJvmArrayAndNestedClassNames() {
        assertSame(int[].class, io.github.lgp547.anydoor.common.util.AnyDoorClassUtil.forName("int[]"));
        assertSame(int[].class, io.github.lgp547.anydoor.common.util.AnyDoorClassUtil.forName("[I"));
        assertSame(String[].class, io.github.lgp547.anydoor.common.util.AnyDoorClassUtil.forName("java.lang.String[]"));
        assertSame(String[].class, io.github.lgp547.anydoor.common.util.AnyDoorClassUtil.forName("[Ljava.lang.String;"));
        assertSame(KotlinTypeFixture.NestedType.class,
                io.github.lgp547.anydoor.common.util.AnyDoorClassUtil.forName("io.github.lgp547.anydoor.test.fixture.KotlinTypeFixture.NestedType"));
    }

    /**
     * 重载方法必须根据擦除后的 JVM 参数类型选择，包括数组参数。
     */
    @Test
    public void resolvesOverloadsByArrayParameterType() throws Exception {
        Method method = io.github.lgp547.anydoor.common.util.AnyDoorClassUtil.getMethod(
                KotlinTypeFixture.class, "overloaded", java.util.Collections.singletonList("int[]"));
        assertEquals("int-array:2", invoke(method, new KotlinTypeFixture(), "[1,2]"));
    }

    /**
     * Kotlin object 调用必须复用其 JVM 单例接收者。
     */
    @Test
    public void invokesObjectMethodOnTheSingleton() throws Exception {
        Object singleton = AnyDoorBeanUtil.getKotlinSingleton(KotlinObjectFixture.class);
        assertSame(KotlinObjectFixture.INSTANCE, singleton);
        AnyDoorRunDto dto = new AnyDoorRunDto();
        dto.setClassName(KotlinObjectFixture.class.getName());
        dto.setMethodName("identity");
        dto.setSync(true);
        dto.setContent(new HashMap<>());
        assertEquals(System.identityHashCode(KotlinObjectFixture.INSTANCE),
                new io.github.lgp547.anydoor.core.AnyDoorService().run(dto));
    }

    /**
     * 静态包装和 companion 实例方法均需使用正确的 JVM 接收者。
     */
    @Test
    public void invokesStaticAndCompanionMethods() throws Exception {
        AnyDoorRunDto staticDto = new AnyDoorRunDto();
        staticDto.setClassName(KotlinStaticOnly.class.getName());
        staticDto.setMethodName("staticEcho");
        staticDto.setParameterTypes(java.util.Collections.singletonList(String.class.getName()));
        staticDto.setSync(true);
        Map<String, Object> staticArgs = new HashMap<>();
        staticArgs.put("args0", "hello");
        staticDto.setContent(staticArgs);
        assertEquals("hello", new io.github.lgp547.anydoor.core.AnyDoorService().run(staticDto));

        Class<?> companionClass = KotlinStaticOnly.class.getDeclaredClasses()[0];
        Object companion = AnyDoorBeanUtil.getKotlinSingleton(companionClass);
        assertSame(KotlinStaticOnly.Companion, companion);
        AnyDoorRunDto companionDto = new AnyDoorRunDto();
        companionDto.setClassName(companionClass.getName());
        companionDto.setMethodName("companionEcho");
        companionDto.setParameterTypes(java.util.Collections.singletonList(String.class.getName()));
        companionDto.setSync(true);
        Map<String, Object> companionArgs = new HashMap<>();
        companionArgs.put("args0", "world");
        companionDto.setContent(companionArgs);
        assertEquals("world", new io.github.lgp547.anydoor.core.AnyDoorService().run(companionDto));
    }

    /**
     * Kotlin 的请求参数使用 argsN；显式 null 可传给 nullable 参数，非 nullable 参数应失败。
     */
    @Test
    public void bindsKotlinArgumentsByPositionAndPreservesNullability() throws Exception {
        Method nullableMethod = KotlinObjectFixture.class.getDeclaredMethod("nullableValue", String.class);
        assertEquals("null", invoke(nullableMethod, KotlinObjectFixture.INSTANCE, "null"));

        Method positionalMethod = KotlinObjectFixture.class.getDeclaredMethod("requiredValue", String.class);
        Map<String, Object> conflictingArgs = new HashMap<>();
        conflictingArgs.put("value", "named");
        conflictingArgs.put("args0", "positional");
        assertEquals("positional", new AnyDoorHandlerMethod(KotlinObjectFixture.INSTANCE, positionalMethod)
                .invokeSync(() -> { }, conflictingArgs));

        Method collidingNamesMethod = KotlinObjectFixture.class.getDeclaredMethod("collidingNames", String.class, String.class);
        Map<String, Object> collidingNamesArgs = new HashMap<>();
        collidingNamesArgs.put("args0", "first-position");
        collidingNamesArgs.put("args1", "second-position");
        assertEquals("first-position|second-position", new AnyDoorHandlerMethod(KotlinObjectFixture.INSTANCE, collidingNamesMethod)
                .invokeSync(() -> { }, collidingNamesArgs));

        Method requiredMethod = KotlinObjectFixture.class.getDeclaredMethod("requiredValue", String.class);
        try {
            invoke(requiredMethod, KotlinObjectFixture.INSTANCE, "null");
            fail("Kotlin 非空参数不应接受 null");
        } catch (RuntimeException expected) {
            assertTrue(expected.getCause() instanceof java.lang.reflect.InvocationTargetException);
        }
    }

    /**
     * Java 方法仍优先使用原有参数名映射。
     */
    @Test
    public void keepsJavaArgumentNameBinding() throws Exception {
        Method method = JavaFixture.class.getDeclaredMethod("echo", String.class);
        Map<String, Object> conflictingArgs = new HashMap<>();
        conflictingArgs.put("value", "named");
        conflictingArgs.put("args0", "positional");
        assertEquals("named", new AnyDoorHandlerMethod(new JavaFixture(), method)
                .invokeSync(() -> { }, conflictingArgs));
    }

    /**
     * Jackson Kotlin 模块应能构造不可变 data class 及其集合参数。
     */
    @Test
    public void deserializesKotlinDataClassAndCollections() throws Exception {
        Object payload = JsonUtil.toJavaBean(
                "{\"name\":\"Ada\",\"age\":37,\"tags\":[\"math\",\"logic\"],\"nickname\":null}",
                io.github.lgp547.anydoor.test.fixture.KotlinPayload.class);
        Method method = KotlinObjectFixture.class.getDeclaredMethod("payloadValue", payload.getClass());
        assertEquals("Ada:37:2:null", invoke(method, KotlinObjectFixture.INSTANCE,
                "{\"name\":\"Ada\",\"age\":37,\"tags\":[\"math\",\"logic\"],\"nickname\":null}"));
    }

    /**
     * 普通 Kotlin 类的私有方法可通过服务入口同步调用。
     */
    @Test
    public void invokesPrivateKotlinMethodThroughService() {
        AnyDoorRunDto dto = runDto(KotlinPrivateMethodFixture.class, "privateEcho", true,
                Collections.<String, Object>singletonMap("args0", "value"));
        assertEquals("private:value", new AnyDoorService().run(dto));
    }

    /**
     * 本地 CGLIB Spring Bean 中的私有 Kotlin 方法应从代理目标对象调用。
     */
    @Test
    public void invokesPrivateKotlinMethodOnSpringCglibBean() throws Exception {
        Field[] stateFields = {
                AnyDoorSpringUtil.class.getDeclaredField("applicationContextList"),
                AnyDoorSpringUtil.class.getDeclaredField("applicationContextsSupplier"),
                AnyDoorSpringUtil.class.getDeclaredField("beanFactorySupplier"),
                AnyDoorSpringUtil.class.getDeclaredField("isInit")
        };
        Object[] previousState = new Object[stateFields.length];
        for (int i = 0; i < stateFields.length; i++) {
            stateFields[i].setAccessible(true);
            previousState[i] = stateFields[i].get(null);
        }

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        try {
            KotlinSpringFixture target = new KotlinSpringFixture();
            ProxyFactory proxyFactory = new ProxyFactory(target);
            proxyFactory.setProxyTargetClass(true);
            proxyFactory.addAdvice((MethodBeforeAdvice) (method, args, bean) -> { });
            final KotlinSpringFixture proxy = (KotlinSpringFixture) proxyFactory.getProxy();
            context.registerBean("kotlinSpringFixture", KotlinSpringFixture.class, () -> proxy);
            context.refresh();
            assertTrue(AnyDoorAopUtil.isCglibProxy(context.getBean(KotlinSpringFixture.class)));
            AnyDoorSpringUtil.initApplicationContexts(() -> new ApplicationContext[]{context}, null);

            AnyDoorRunDto dto = runDto(KotlinSpringFixture.class, "privateEcho", true,
                    Collections.<String, Object>singletonMap("args0", "value"));
            assertEquals("spring:value:1", new AnyDoorService().run(dto));
            assertEquals(1, context.getBean(KotlinSpringFixture.class).invocationCount());
        } finally {
            try {
                for (int i = 0; i < stateFields.length; i++) {
                    stateFields[i].set(null, previousState[i]);
                }
            } finally {
                context.close();
            }
        }
    }

    /**
     * 服务入口的同步、异步及批量调度应完成全部 Kotlin 调用。
     */
    @Test
    public void runsKotlinMethodsSynchronouslyAsynchronouslyAndInBatches() {
        AnyDoorService service = new AnyDoorService();
        KotlinScheduleFixture.INSTANCE.reset(1);
        assertEquals("sync:1", service.run(runDto(KotlinScheduleFixture.class, "record", true,
                Collections.<String, Object>singletonMap("args0", "sync"))));

        KotlinScheduleFixture.INSTANCE.reset(3);
        AnyDoorRunDto syncBatch = runDto(KotlinScheduleFixture.class, "record", true,
                Arrays.asList(args("sync-a"), args("sync-b"), args("sync-c")));
        syncBatch.setConcurrent(false);
        assertNull(service.run(syncBatch));
        assertEquals(3, KotlinScheduleFixture.INSTANCE.invocationCount());

        KotlinScheduleFixture.INSTANCE.reset(1);
        AnyDoorRunDto async = runDto(KotlinScheduleFixture.class, "record", false,
                Collections.<String, Object>singletonMap("args0", "async"));
        assertNull(service.run(async));
        assertTrue("单条异步调用应在时限内完成", KotlinScheduleFixture.INSTANCE.awaitInvocations(5, TimeUnit.SECONDS));
        assertEquals(1, KotlinScheduleFixture.INSTANCE.invocationCount());

        KotlinScheduleFixture.INSTANCE.reset(3);
        AnyDoorRunDto asyncBatch = runDto(KotlinScheduleFixture.class, "record", false,
                Arrays.asList(args("async-a"), args("async-b"), args("async-c")));
        asyncBatch.setConcurrent(false);
        assertNull(service.run(asyncBatch));
        assertTrue("异步批量调用应在时限内完成", KotlinScheduleFixture.INSTANCE.awaitInvocations(5, TimeUnit.SECONDS));
        assertEquals(3, KotlinScheduleFixture.INSTANCE.invocationCount());

        KotlinScheduleFixture.INSTANCE.reset(3);
        AnyDoorRunDto concurrentBatch = runDto(KotlinScheduleFixture.class, "record", false,
                Arrays.asList(args("concurrent-a"), args("concurrent-b"), args("concurrent-c")));
        concurrentBatch.setConcurrent(true);
        assertNull(service.run(concurrentBatch));
        assertTrue("并发批量调用应在时限内完成", KotlinScheduleFixture.INSTANCE.awaitInvocations(5, TimeUnit.SECONDS));
        assertEquals(3, KotlinScheduleFixture.INSTANCE.invocationCount());
    }

    /**
     * Handler API 返回的异步与批量 CompletableFuture 应在所有调用结束后完成。
     */
    @Test
    public void completesHandlerFuturesForKotlinCalls() throws Exception {
        Method method = KotlinScheduleFixture.class.getDeclaredMethod("record", String.class);
        AnyDoorHandlerMethod handler = new AnyDoorHandlerMethod(KotlinScheduleFixture.INSTANCE, method);

        KotlinScheduleFixture.INSTANCE.reset(1);
        assertEquals("future:1", handler.invokeAsync(() -> { }, args("future")).get(5, TimeUnit.SECONDS));
        assertEquals(1, KotlinScheduleFixture.INSTANCE.invocationCount());

        KotlinScheduleFixture.INSTANCE.reset(3);
        CompletableFuture<Void> batchFuture = handler.parallelInvokeAsync(() -> { },
                Arrays.asList(args("batch-a"), args("batch-b"), args("batch-c")), 3, (index, result) -> { });
        batchFuture.get(5, TimeUnit.SECONDS);
        assertEquals(3, KotlinScheduleFixture.INSTANCE.invocationCount());

        KotlinScheduleFixture.INSTANCE.reset(3);
        java.util.List<CompletableFuture<Object>> concurrentFutures = handler.concurrentInvokeAsync(() -> { },
                Arrays.asList(args("parallel-a"), args("parallel-b"), args("parallel-c")), 3,
                (index, result) -> { }, (index, failure) -> fail("并发调用失败：" + failure.getMessage()));
        CompletableFuture.allOf(concurrentFutures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
        assertEquals(3, KotlinScheduleFixture.INSTANCE.invocationCount());
    }

    private static AnyDoorRunDto runDto(Class<?> targetClass, String methodName, boolean sync, Object content) {
        AnyDoorRunDto dto = new AnyDoorRunDto();
        dto.setClassName(targetClass.getName());
        dto.setMethodName(methodName);
        dto.setSync(sync);
        dto.setContent(content);
        return dto;
    }

    private static Map<String, Object> args(String value) {
        return Collections.<String, Object>singletonMap("args0", value);
    }

    private static Object invoke(Method method, Object bean, String jsonValue) {
        Map<String, Object> content = new HashMap<>();
        content.put("args0", JsonUtil.toJavaBean(jsonValue, Object.class));
        return new AnyDoorHandlerMethod(bean, method).invokeSync(() -> { }, content);
    }

    private static class JavaFixture {

        /**
         * 返回按 Java 参数名绑定的值。
         *
         * @param value 输入值
         * @return 输入值
         */
        public String echo(String value) {
            return value;
        }
    }
}
