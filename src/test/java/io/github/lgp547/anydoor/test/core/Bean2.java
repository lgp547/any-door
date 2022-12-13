package io.github.lgp547.anydoor.test.core;

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
}
