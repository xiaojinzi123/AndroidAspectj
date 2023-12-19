package com.xiaojinzi.aspectj.plugin.config

data class AspectjConfig(
    val ignoreOutputJar: Boolean?,
    val loopSolveCount: Int?,
    val logError: Boolean,
    val sourceCompatibility: String,
    val targetCompatibility: String,
)
