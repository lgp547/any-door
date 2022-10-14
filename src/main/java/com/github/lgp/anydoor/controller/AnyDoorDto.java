package com.github.lgp.anydoor.controller;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * className      全链路名
 * methodName     方法名
 * content        入参，要求是json类型，允许为null
 * parameterTypes 参数类型
 */
public class AnyDoorDto {

    @NonNull
    private String className;

    @NonNull
    private String methodName;

    private String content;

    private List<String> parameterTypes;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        return "AnyDoorDto{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", content='" + content + '\'' +
                ", parameterTypes=" + parameterTypes +
                '}';
    }

    public void verify() {
        Assert.notNull(className, "className is required");
        Assert.notNull(methodName, "methodName is required");
    }

}
