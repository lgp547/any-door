package io.github.lgp547.anydoor.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.joegreen.lambdaFromString.LambdaCreationException;
import pl.joegreen.lambdaFromString.LambdaFactory;
import pl.joegreen.lambdaFromString.TypeReference;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public class LambdaUtil {

    private static final Logger log = LoggerFactory.getLogger(LambdaUtil.class);

    private static final LambdaFactory lambdaFactory = LambdaFactory.get();

    public static <T> T compileExpression(String value,Type parameterType){
        try {
            return lambdaFactory.createLambda(value,new TypeReference<T>(parameterType){});
        } catch (LambdaCreationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T runNotExc(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.debug("runNotExc exception", e);
            return null;
        }
    }

}