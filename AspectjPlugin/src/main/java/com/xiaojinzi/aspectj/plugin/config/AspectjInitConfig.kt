package com.xiaojinzi.aspectj.plugin.config

open class AspectjInitConfig(
    open var enable: Boolean? = null,
    open var enableAspectLog: Boolean? = null,
    open var enableAdvancedMatch: Boolean? = null,
    open var includePackagePrefixSet: Set<String>? = null,
    open var excludePackagePrefixSet: Set<String>? = null,
    open var includePackagePatternSet: Set<String>? = null,
    open var excludePackagePatternSet: Set<String>? = null,
) {
    override fun toString(): String {
        return "AspectjInitConfig(enable=$enable, enableAspectLog=$enableAspectLog, includePackagePrefixSet=$includePackagePrefixSet, excludePackagePrefixSet=$excludePackagePrefixSet)"
    }
}