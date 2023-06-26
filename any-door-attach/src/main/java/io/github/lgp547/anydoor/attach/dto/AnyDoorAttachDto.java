package io.github.lgp547.anydoor.attach.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * className      全链路名
 * methodName     方法名
 * content        入参，要求是json类型，允许为null
 * parameterTypes 参数类型 （若是方法名是唯一的，这个parameterTypes是可选填）
 * isSync         是否同步（默认异步）
 * jarPaths       任意门core-jar包相关路径(用于加载类)
 */
public class AnyDoorAttachDto {

    private String className;

    private String methodName;

    private List<String> parameterTypes;

    private List<String> jarPaths;

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

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }


    public List<String> getJarPaths() {
        return jarPaths;
    }

    public void setJarPaths(List<String> jarPaths) {
        this.jarPaths = jarPaths;
    }

    @Override
    public String toString() {
        return "AnyDoorAttachDto{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + parameterTypes +
                ", jarPaths=" + jarPaths +
                '}';
    }

    public boolean verifyPass() {
        return className != null && methodName != null && jarPaths != null;
    }

    public static AnyDoorAttachDto parseObj(String anyDoorDtoStr) {
        String className = substringBetween(anyDoorDtoStr, "\"className\":\"", "\"");
        String methodName = substringBetween(anyDoorDtoStr, "\"methodName\":\"", "\"");
        List<String> parameterTypes = parseList(anyDoorDtoStr, "parameterTypes");
        List<String> jarPaths = parseList(anyDoorDtoStr, "jarPaths");

        AnyDoorAttachDto anyDoorAttachDto = new AnyDoorAttachDto();
        anyDoorAttachDto.setClassName(className);
        anyDoorAttachDto.setMethodName(methodName);
        anyDoorAttachDto.setParameterTypes(parameterTypes);
        anyDoorAttachDto.setJarPaths(jarPaths);
        return anyDoorAttachDto;
    }


    public static List<String> parseList(String anyDoorDtoStr, String paramName) {
        String s = substringBetween(anyDoorDtoStr, "\"" + paramName + "\":[", "]");
        if (!isEmpty(s)) {
            return Arrays.stream(s.split(",")).map(i -> removeEnd(removeStart(i, "\""), "\"")).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static String substringBetween(final String str, final String open, final String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        final int start = str.indexOf(open);
        if (start != -1) {
            final int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    public static String removeEnd(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    public static String removeStart(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)) {
            return str.substring(remove.length());
        }
        return str;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

}
