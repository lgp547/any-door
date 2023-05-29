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
