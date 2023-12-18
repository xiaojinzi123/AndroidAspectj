package com.xiaojinzi.aspectj.plugin.config

data class AspectjConfig(
    val enableLoopSolve: Boolean? = null,
    val sourceCompatibility: String,
    val targetCompatibility: String,
)
