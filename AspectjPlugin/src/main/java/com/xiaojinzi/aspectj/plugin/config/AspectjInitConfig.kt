package com.xiaojinzi.aspectj.plugin.config

open class AspectjInitConfig(
    open var enable: Boolean? = null,
    open var enableLoopSolve: Boolean? = null,
) {

    override fun toString(): String {
        return "AspectjInitConfig(enable=$enable)"
    }

}