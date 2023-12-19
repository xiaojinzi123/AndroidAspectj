package com.xiaojinzi.aspectj.user;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class UserAspectjDec {

    private static final String POINTCUT_METHOD1 =
            "execution(* com.xiaojinzi.aspectj.app1.module.main.view.MainAct.getViewModelClass(..))";

    @After(POINTCUT_METHOD1)
    public void moduleUserAspectjForKotlin(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        Log.d(
                "TestAspectj",
                "UserAspectjDec After 1211212 moduleUserAspectjForKotlin className + " + className + ",methodName = " + methodName
        );
    }

}
