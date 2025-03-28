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
 
package io.github.lgp547.anydoor.test.util;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Objects;

public class AssertUtil {
    
    public static void assertIsEquals(Object a, Object b) {
        Assert.isTrue(Objects.equals(a, b), "fail");
    }
    
    public static void assertIsTrue(boolean expression) {
        Assert.isTrue(expression, "fail");
    }
    
    public static void assertIsNull(@Nullable Object object) {
        Assert.isNull(object, "fail");
    }
    
    public static void assertNotNull(@Nullable Object object) {
        Assert.notNull(object, "fail");
    }
}
