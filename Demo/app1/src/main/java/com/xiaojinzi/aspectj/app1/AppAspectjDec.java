package com.xiaojinzi.aspectj.app1;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class AppAspectjDec {

    private static final String POINTCUT_METHOD1 =
            "execution(* com.xiaojinzi.aspectj.app1.TestAspectjForJava.test(..))";

    private static final String POINTCUT_METHOD2 =
            "execution(* com.xiaojinzi.aspectj.app1.TestAspectjForKotlin.test(..))";

    private static final String POINTCUT_METHOD3 =
            "execution(* com.xiaojinzi.aspectj.app1.module.main.view.MainAct.onCreate(..))";

    @Around(POINTCUT_METHOD1)
    public Object testAspectjForJava(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String className = methodSignature.getDeclaringType().getSimpleName();
            String methodName = methodSignature.getName();
            Log.d(
                    "TestAspectj",
                    "testAspectjForJava className + " + className + ",methodName = " + methodName
            );
            return joinPoint.proceed();
        } catch (Exception e) {
            return joinPoint.proceed();
        }
    }

    @Around(POINTCUT_METHOD2)
    public Object testAspectjForKotlin(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String className = methodSignature.getDeclaringType().getSimpleName();
            String methodName = methodSignature.getName();
            Log.d(
                    "TestAspectj",
                    "testAspectjForKotlin className + " + className + ",methodName = " + methodName
            );
            return joinPoint.proceed();
        } catch (Exception e) {
            return joinPoint.proceed();
        }
    }

    @Around(POINTCUT_METHOD3)
    public Object moduleUserAspectjForKotlin(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String className = methodSignature.getDeclaringType().getSimpleName();
            String methodName = methodSignature.getName();
            Log.d(
                    "TestAspectj",
                    "Around moduleUserAspectjForKotlin className + " + className + ",methodName = " + methodName
            );
            return joinPoint.proceed();
        } catch (Exception e) {
            return joinPoint.proceed();
        }
    }

}
