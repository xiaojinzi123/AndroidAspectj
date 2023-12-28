package com.xiaojinzi.aspectj.api.anno

/**
 * 标记的类需要忽略织入
 * 这个类开头的都要
 */
@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.BINARY)
annotation class AspectWeaveIgnore