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
 
package io.github.lgp547.anydoor.test.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    
    private static final String privateTag = "privateTag";
    
    public static final String publicTag = "publicTag";
    
    public String getPublicTag() {
        return publicTag;
    }
    
    private Integer id;
    
    private String name;
    
    private LocalDateTime createTime;
    
    private LocalDate localDate;
    
    public User() {
    }
    
    public User(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public User(Integer id, String name, LocalDateTime createTime, LocalDate localDate) {
        this.id = id;
        this.name = name;
        this.createTime = createTime;
        this.localDate = localDate;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDate getLocalDate() {
        return localDate;
    }
    
    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createTime=" + createTime +
                ", localDate=" + localDate +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(createTime, user.createTime) && Objects.equals(localDate, user.localDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, createTime, localDate);
    }
}
