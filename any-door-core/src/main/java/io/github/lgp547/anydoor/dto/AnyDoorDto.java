package io.github.lgp547.anydoor.dto;

import io.github.lgp547.anydoor.util.ClassUtil;
import io.github.lgp547.anydoor.util.JsonUtil;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * className      全链路名
 * methodName     方法名
 * content        入参，要求是json类型，允许为null
 * parameterTypes 参数类型 （若是方法名是唯一的，这个parameterTypes是可选填）
 * isSync         是否同步（默认异步）
 */
public class AnyDoorDto {

    @NonNull
    private String className;

    @NonNull
    private String methodName;

    private Object content;

    private List<String> parameterTypes;

    private Boolean isSync;

    private Integer num;

    private Boolean concurrent;

    public String getClassName() {
        return className;
    }

    public Class<?> getClazz() {
        return ClassUtil.forName(className);
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

    public Object getContent() {
        return content;
    }

    public Map<String, Object> getContentMap() {
        return JsonUtil.toMap(JsonUtil.toStrNotExc(content));
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Boolean getSync() {
        return isSync;
    }

    public void setSync(Boolean sync) {
        isSync = sync;
    }

    public Integer getNum() {
        return num == null || num < 1 ? 1 : num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public boolean getConcurrent() {
        return Objects.equals(concurrent, Boolean.TRUE);
    }

    public void setConcurrent(Boolean concurrent) {
        this.concurrent = concurrent;
    }

    @Override
    public String toString() {
        return "AnyDoorDto{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", content=" + content +
                ", parameterTypes=" + parameterTypes +
                ", isSync=" + isSync +
                ", runNum=" + num +
                ", isConcurrent=" + concurrent +
                '}';
    }

    public void verify() {
        Assert.notNull(className, "className is required");
        Assert.notNull(methodName, "methodName is required");
    }

}
