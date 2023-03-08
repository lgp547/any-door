package io.github.lgp547.anydoor.test.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class Bean3Aspect {
    @Pointcut("execution(* io.github.lgp547.anydoor.test.core.AopBean.*(..))")
    public void bean3Pointcut() {

    }

    @Before("bean3Pointcut()")
    public void Before() {
        System.out.println("Before bean3Aspect");
    }
}
