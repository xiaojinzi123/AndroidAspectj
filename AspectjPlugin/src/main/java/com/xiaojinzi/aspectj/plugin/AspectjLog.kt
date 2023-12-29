package com.xiaojinzi.aspectj.plugin

object AspectjLog {

    var enable: Boolean = false

    fun d(
        content: String,
    ) {
        if (enable) {
            println("${AspectjPlugin.TAG}: $content")
        }
    }

}