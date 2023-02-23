package io.github.lgp547.anydoor.test.core;

import java.util.function.BiFunction;
import static io.github.lgp547.anydoor.test.util.AssertUtil.assertIsTrue;
import static io.github.lgp547.anydoor.test.util.AssertUtil.assertNotNull;

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
        assertNotNull(func);
        String apply = func.apply(a, b);
        assertIsTrue("Hello World".equals(apply));
        System.out.println(apply);
        return apply;
    }

}
