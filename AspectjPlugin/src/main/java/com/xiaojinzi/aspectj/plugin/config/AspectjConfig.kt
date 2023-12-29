package com.xiaojinzi.aspectj.plugin.config

data class AspectjConfig(
    val enableAdvancedMatch: Boolean,
    val sourceCompatibility: String,
    val targetCompatibility: String,
    val includePackagePrefixSet: Set<String>,
    val excludePackagePrefixSet: Set<String>,
    val includePackagePatternSet: Set<String>,
    val excludePackagePatternSet: Set<String>,
    val includePackagePrefixFormatSet: Set<String> = includePackagePrefixSet.map {
        it.replace(
            oldChar = '.', '/',
        )
    }.toSet(),
    val excludePackagePrefixFormatSet: Set<String> = excludePackagePrefixSet.map {
        it.replace(
            oldChar = '.', '/',
        )
    }.toSet(),
    val includePackagePatternFormatSet: Set<String> = includePackagePatternSet.map {
        it.replace(
            oldChar = '.', '/',
        )
    }.toSet(),
    val excludePackagePatternFormatSet: Set<String> = excludePackagePatternSet.map {
        it.replace(
            oldChar = '.', '/',
        )
    }.toSet(),
)
