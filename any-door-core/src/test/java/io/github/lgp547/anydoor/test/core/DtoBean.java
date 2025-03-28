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
 
package io.github.lgp547.anydoor.test.core;

import java.util.function.BiFunction;

import static io.github.lgp547.anydoor.test.util.AssertUtil.assertIsTrue;
import static io.github.lgp547.anydoor.test.util.AssertUtil.assertNotNull;

/**
 * 无注册Bean
 */
public class DtoBean {
    
    public void noParam() {
        System.out.println("noParam");
    }
    
    public static String noParamStatic() {
        String noParamStatic = "noParamStatic";
        System.out.println(noParamStatic);
        return noParamStatic;
    }
    public static String doSomething(BiFunction<String, String, String> func, String a, String b) {
        assertNotNull(func);
        String apply = func.apply(a, b);
        assertIsTrue("Hello World".equals(apply));
        System.out.println(apply);
        return apply;
    }
    
}
