package io.github.lgp547.anydoor.test.core;

import java.util.function.BiFunction;

/**
 * 无注册Bean
 */
public class Bean2 {

    public void noParam() {
        System.out.println("noParam");
    }

    public static String noParamStatic() {
        String noParamStatic = "noParamStatic";
        System.out.println(noParamStatic);
        return noParamStatic;
    }
    public static String doSomething(BiFunction<String, String, String> func, String a, String b) {
        String apply = func.apply(a, b);
        System.out.println(apply);
        return apply;
    }

}
