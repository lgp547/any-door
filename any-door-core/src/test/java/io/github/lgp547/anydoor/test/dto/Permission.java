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

import java.util.Objects;

public class Permission {
    
    private Long id;
    private String text;
    
    public Long getId() {
        return id;
    }
    
    public String getText() {
        return text;
    }
    
    // public Permission() {
    // }
    
    public Permission(Long id, String text) {
        this.id = id;
        this.text = text;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Permission that = (Permission) o;
        return Objects.equals(id, that.id) && Objects.equals(text, that.text);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }
    
    public static class PermissionBuilder {
        
        private Long id;
        private String text;
        
        PermissionBuilder() {
        }
        
        public Permission.PermissionBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Permission.PermissionBuilder text(String text) {
            this.text = text;
            return this;
        }
        
        public Permission build() {
            return new Permission(this.id, this.text);
        }
        
        public String toString() {
            return "Permission.PermissionBuilder(id=" + this.id + ", text=" + this.text + ")";
        }
    }
    
}
