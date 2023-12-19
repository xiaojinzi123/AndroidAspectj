package com.xiaojinzi.aspectj.plugin.config

open class AspectjInitConfig(
    open var enable: Boolean? = null,
    open var logError: Boolean? = null,
    open var loopSolveCount: Int? = null,
    open var ignoreOutputJar: Boolean? = null,
) {
    override fun toString(): String {
        return "AspectjInitConfig(enable=$enable, logError=$logError, loopSolveCount=$loopSolveCount)"
    }
}