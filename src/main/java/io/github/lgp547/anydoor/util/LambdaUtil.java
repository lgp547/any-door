package io.github.lgp547.anydoor.util;


import pl.joegreen.lambdaFromString.LambdaCreationException;
import pl.joegreen.lambdaFromString.LambdaFactory;
import pl.joegreen.lambdaFromString.TypeReference;

import java.lang.reflect.Type;

public class LambdaUtil {

    private static final LambdaFactory lambdaFactory = LambdaFactory.get();

    public static <T> T compileExpression(String value,Type parameterType){
        try {
            return lambdaFactory.createLambda(value,new TypeReference<T>(parameterType){});
        } catch (LambdaCreationException e) {
            throw new IllegalArgumentException(e);
        }
    }

}