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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.lgp547.anydoor.test.dto.Permission;
import io.github.lgp547.anydoor.test.dto.Role;
import io.github.lgp547.anydoor.test.dto.User;

import io.github.lgp547.anydoor.util.jackson.AnyDoorTimeDeserializer;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import static io.github.lgp547.anydoor.test.util.AssertUtil.assertIsEquals;
import static io.github.lgp547.anydoor.test.util.AssertUtil.assertIsNull;
import static io.github.lgp547.anydoor.test.util.AssertUtil.assertIsTrue;
import static io.github.lgp547.anydoor.test.util.AssertUtil.assertNotNull;

@RestController
public class Bean {
    
    public static final Integer id = 1;
    
    public static final String name = "AnyDoorUserName";
    
    public static final User user = new User(1, "AnyDoorUserName1");
    
    public static final List<String> strings = new ArrayList<String>() {
        
        {
            add("AnyDoorStr1");
            add("AnyDoorStr2");
            add("AnyDoorStr1");
        }
    };
    
    public static final String[] strArray = new String[]{"a", "b", "c"};
    
    public static final List<User> users = new ArrayList<User>() {
        
        {
            add(new User(2, "2", LocalDateTime.now(), LocalDate.now()));
            add(new User(3, "3"));
        }
    };
    
    public static final List<Long> longs = new ArrayList<Long>() {
        
        {
            add(1L);
            add(2L);
            add(3L);
        }
    };
    
    public static final String emptyUser = "";
    
    public static final String emptyNum = "0";
    
    public static final Role role = new Role(100L, "roleName", users);
    
    public static final Long dateTimeByLong = 1L;
    
    public static final String dateTimeByISO = "2023-01-01T00:00:00";
    
    public static final String dateTimeByCom = "2023-01-01 00:00:00";
    
    public static final String dateTimeByCom1 = "2023-01-02";
    
    public static final Permission permission = new Permission(1L, "permissionName");
    
    public static final int intValue = 1;
    
    public static final long longValue = 1L;
    
    public static final float floatValue = 1.0f;
    
    public static final double doubleValue = 1.0d;
    
    public static final boolean booleanValue = true;
    
    public static final char charValue = 'a';
    
    public static final byte byteValue = 1;
    
    public static final short shortValue = 1;
    
    public static JsonNode getContent() {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("id", id);
        jsonNode.put("name", name);
        jsonNode.putPOJO("user", user);
        jsonNode.putPOJO("strings", strings);
        jsonNode.putPOJO("strArray", strArray);
        jsonNode.putPOJO("users", users);
        jsonNode.put("emptyUser", emptyUser);
        jsonNode.put("emptyNum", emptyNum);
        String s = null;
        jsonNode.put("nameNull", s);
        jsonNode.putPOJO("role", role);
        jsonNode.put("func", "(a,b) -> a + b");
        jsonNode.putPOJO("a", "Hello");
        jsonNode.putPOJO("b", " World");
        jsonNode.put("dateTimeByLong", dateTimeByLong);
        jsonNode.put("dateTimeByISO", dateTimeByISO);
        jsonNode.put("dateTimeByCom", dateTimeByCom);
        jsonNode.put("dateTimeByCom1", dateTimeByCom1);
        jsonNode.putPOJO("longs", longs);
        jsonNode.putPOJO("permission", permission);
        jsonNode.put("intValue", intValue);
        jsonNode.put("longValue", longValue);
        jsonNode.put("floatValue", floatValue);
        jsonNode.put("doubleValue", doubleValue);
        jsonNode.put("booleanValue", booleanValue);
        jsonNode.put("charValue", charValue);
        jsonNode.put("byteValue", byteValue);
        jsonNode.put("shortValue", shortValue);
        return jsonNode;
    }
    
    private void noParamPrivate() {
        System.out.println("noParamPrivate");
    }
    
    public static void noParamStatic() {
        System.out.println("noParamStatic");
    }
    
    public void noParam() {
        System.out.println("noParam");
    }
    
    public String only(String name) {
        System.out.println(name);
        return name;
    }
    
    public void test(Integer num, String name) {
        System.out.println("num:" + num);
        System.out.println("name:" + name);
    }
    
    public String oneParam(String name) {
        assertIsTrue(Bean.name.equals(name));
        return name;
    }
    
    public void oneParamNull(String nameNull) {
        Assert.isNull(nameNull);
    }
    
    private String oneParamPrivate(String name) {
        assertIsTrue(Bean.name.equals(name));
        return name;
    }
    
    public Long oneParam(Long nullParam) {
        assertIsNull(nullParam);
        return nullParam;
    }
    
    public Integer oneParamEmpty(Integer emptyNum) {
        assertNotNull(emptyNum);
        return emptyNum;
    }
    
    public User oneParamEmpty(User emptyUser) {
        assertNotNull(emptyUser);
        assertIsNull(emptyUser.getId());
        assertIsNull(emptyUser.getName());
        return emptyUser;
    }
    
    public User oneParam(User user) {
        assertIsTrue(Bean.user.equals(user));
        return user;
    }
    
    public List<String> oneParam1(List<String> strings) {
        assertIsTrue(Bean.strings.equals(strings));
        return strings;
    }
    
    public String[] oneParam1(String[] strArray) {
        assertIsTrue(Arrays.equals(Bean.strArray, strArray));
        return strArray;
    }
    
    public Set<String> oneParam1Set(Set<String> strings) {
        HashSet<String> set = new HashSet<>(Bean.strings);
        assertIsTrue(set.equals(strings));
        return strings;
    }
    
    public List<User> oneParam2(List<User> users) {
        Objects.requireNonNull(users);
        for (int i = 0; i < Bean.users.size(); i++) {
            assertIsTrue(Bean.users.get(i).equals(users.get(i)));
        }
        return users;
    }
    
    public JsonNode multipleParam(String name, Integer id) {
        assertIsTrue(Bean.name.equals(name));
        assertIsTrue(Bean.id.equals(id));
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("name", name);
        jsonNode.put("id", id);
        return jsonNode;
    }
    
    public JsonNode multipleParam(Integer id, String name) {
        assertIsTrue(Bean.name.equals(name));
        assertIsTrue(Bean.id.equals(id));
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("name", name);
        jsonNode.put("id", id);
        return jsonNode;
    }
    
    public void exception() {
        System.out.println("bean test exception");
        throw new RuntimeException("bean test exception");
    }
    
    public void role(Role role) {
        assertIsTrue(Bean.role.equals(role));
    }
    
    /**
     * 重载的私有方法
     */
    private void role() {
        System.out.println("执行重载的私有方法");
    }
    
    /**
     * 测试转LDT
     */
    public void testDate(LocalDateTime dateTimeByLong, LocalDateTime dateTimeByISO, LocalDateTime dateTimeByCom,
                         LocalDateTime dateTimeByCom1) {
        
        assertIsTrue(Objects.equals(dateTimeByLong, LocalDateTime.ofInstant(Instant.ofEpochMilli(Bean.dateTimeByLong), ZoneId.systemDefault())));
        
        assertIsEquals(dateTimeByISO, LocalDateTime.parse(Bean.dateTimeByISO, DateTimeFormatter.ISO_DATE_TIME));
        
        assertIsEquals(dateTimeByCom, LocalDateTime.parse(Bean.dateTimeByCom, AnyDoorTimeDeserializer.DATETIME_FORMAT));
        
        assertIsEquals(dateTimeByCom1, LocalDateTime.parse(Bean.dateTimeByCom1 + " 00:00:00", AnyDoorTimeDeserializer.DATETIME_FORMAT));
        
    }
    
    public void listLong(List<Long> longs) {
        assertIsTrue(Bean.longs.equals(longs));
    }
    
    public void testBuilder(Permission permission) {
        assertIsEquals(Bean.permission, permission);
    }
    
    public void testPrimitiveType(int intValue, long longValue, double doubleValue, float floatValue,
                                  boolean booleanValue, char charValue, byte byteValue, short shortValue) {
        assertIsEquals(intValue, Bean.intValue);
        assertIsEquals(longValue, Bean.longValue);
        assertIsEquals(doubleValue, Bean.doubleValue);
        assertIsEquals(floatValue, Bean.floatValue);
        assertIsEquals(booleanValue, Bean.booleanValue);
        assertIsEquals(charValue, Bean.charValue);
        assertIsEquals(byteValue, Bean.byteValue);
        assertIsEquals(shortValue, Bean.shortValue);
    }
    
    public void testPrimitiveType(int intValue) {
        assertIsEquals(intValue, Bean.intValue);
    }
    
}
