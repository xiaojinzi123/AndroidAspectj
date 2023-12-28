package com.xiaojinzi.aspectj.app1

import android.util.Log
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature

@Aspect
class AppAspectjDec {

    companion object {
        // 匹配
        private const val POINTCUT_METHOD1 =
            "execution(* com.xiaojinzi.aspectj.app1.App1.onCreate())"
    }

    @Around(POINTCUT_METHOD1)
    @Throws(Throwable::class)
    fun appOnCreate(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = joinPoint.signature
        val className = methodSignature.declaringType.simpleName
        val methodName = methodSignature.name
        Log.d(
            "appOnCreate",
            "className = $className,methodName = $methodName"
        )
        return joinPoint.proceed()
    }

}
