package com.xiaojinzi.aspectj.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.xiaojinzi.aspectj.plugin.config.AspectjConfig
import com.xiaojinzi.aspectj.plugin.config.AspectjInitConfig
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.register
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class AspectjPlugin : Plugin<Project> {

    companion object {
        const val TAG = "AspectjPlugin"
        const val EXT_ASPECTJ_CONFIG = "aspectjConfig"
    }

    // @CacheableTask
    abstract class AspectjTask : DefaultTask() {

        @get:InputFiles
        abstract val allJars: ListProperty<RegularFile>

        @get:InputFiles
        abstract val allDirectories: ListProperty<Directory>

        @get:OutputFile
        abstract val outputFile: RegularFileProperty

        @get:Classpath
        abstract val bootClasspath: ListProperty<RegularFile>

        @get:CompileClasspath
        abstract var classpath: FileCollection

        private val cacheFolder = File(project.buildDir, "aspectj")

        private val logger = project.logger

        private val aspectjConfig = project.extra[EXT_ASPECTJ_CONFIG] as AspectjConfig

        @TaskAction
        fun taskAction() {

            println("$TAG, allDirectories = ${allDirectories.get()}")
            println("$TAG, cacheFolder = $cacheFolder")

            val outputFile = outputFile.asFile.get()
            val allJarList = allJars.get()

            val inputs: List<java.nio.file.Path> =
                (allJarList + allDirectories.get()).map { it.asFile.toPath() }

            val isContainsOutputFile = allJarList
                .find {
                    it.asFile == outputFile
                } != null
            println("$TAG, isContainsOutputFile = $isContainsOutputFile")

            val isLoop = aspectjConfig.enableLoopSolve?: false

            val action = {
                val targetCacheFolder = File(cacheFolder, "${System.currentTimeMillis()}")
                println("$TAG, targetCacheFolder = $targetCacheFolder")

                val handler = MessageHandler(true)
                org.aspectj.tools.ajc.Main().run(
                    arrayOf(
                        "-showWeaveInfo",
                        "-source", aspectjConfig.sourceCompatibility,
                        "-target", aspectjConfig.targetCompatibility,
                        "-inpath",
                        inputs
                            .joinToString(
                                separator = File.pathSeparator,
                            ).apply {
                                println("$TAG, inputs.size = ${inputs.size} inputs = $this")
                            },
                        "-aspectpath",
                        classpath.asPath.apply {
                            println("$TAG, javaCompile.classpath = $this")
                        },
                        /*"outjar", outputFile.path.apply {
                            println("${AspectjPlugin.TAG}, outputFile = $this")
                        },*/
                        "-d",
                        targetCacheFolder.path.apply {
                            println("$TAG, destinationDirectory = $this")
                        },
                        "-classpath", classpath.asPath,
                        "-bootclasspath",
                        bootClasspath
                            .get()
                            .joinToString(
                                separator = File.pathSeparator,
                            ).apply {
                                println("$TAG, bootclasspath = $this")
                            },
                    ),
                    handler,
                )

                handler.getMessages(
                    null, true
                ).forEach { message ->
                    when (message.kind) {
                        IMessage.INFO -> logger.info(message.message, message.thrown)
                        IMessage.DEBUG -> logger.debug(message.message, message.thrown)
                        IMessage.WARNING -> logger.warn(message.message, message.thrown)
                        IMessage.ERROR,
                        IMessage.FAIL,
                        IMessage.ABORT -> {
                            logger.error(message.message, message.thrown)
                            throw message.thrown
                        }

                        else -> logger.error(message.message, message.thrown)
                    }
                }
                targetCacheFolder
            }

            val targetCacheFolder = if (isLoop) {
                var resultFolder: File
                var count = 1
                while (true) {
                    try {
                        println("$TAG, 第 $count 次执行")
                        resultFolder = action.invoke()
                        break
                    } catch (_: Exception) {
                    }
                    count++
                }
                resultFolder
            } else {
                action.invoke()
            }

            println("$TAG, 准备合并到 ${outputFile.name} 中, 当前是否存在：${outputFile.exists()}")

            outputFile.delete()
            JarOutputStream(
                BufferedOutputStream(
                    FileOutputStream(
                        outputFile
                    )
                )
            ).use { jarOutput ->
                targetCacheFolder.walk().forEach { file ->
                    if (file.isFile) {
                        if (file != outputFile) {
                            // println("${AspectjPlugin.TAG}, 准备合并 ${file.path}")
                            val relativePath =
                                targetCacheFolder.toURI().relativize(file.toURI()).path
                            jarOutput.putNextEntry(
                                JarEntry(
                                    relativePath.replace(
                                        File.separatorChar,
                                        '/'
                                    )
                                )
                            )
                            file.inputStream().use { inputStream ->
                                inputStream.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                    }
                }
            }

        }

    }

    override fun apply(project: Project) {

        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        val extensionsMap = (project.extensions as? ExtensionContainerInternal)?.asMap
        // project.plugins.withType(AppPlugin::class.java)
        println("$TAG, isApp = $isApp, project = ${project.name}, project.extensions = $extensionsMap")

        if (!isApp) {
            return
        }

        // 添加扩展
        project.extensions.add("aspectjConfig", AspectjInitConfig::class.java)

        val androidComponents = project.extensions
            .findByType(AndroidComponentsExtension::class.java)

        androidComponents?.onVariants { variant ->

            val aspectjConfig = project.extensions.findByType(AspectjInitConfig::class.java)
            println("$TAG, variant.name = ${variant.name} aspectjConfig = $aspectjConfig")

            val isAspectjEnable = aspectjConfig?.enable ?: true
            if (isAspectjEnable) {

                val baseAppModuleExtension =
                    project.extensions.findByType(BaseAppModuleExtension::class.java)
                        ?: return@onVariants

                // 存入 extra
                project
                    .extra
                    .set(
                        EXT_ASPECTJ_CONFIG,
                        AspectjConfig(
                            enableLoopSolve = aspectjConfig?.enableLoopSolve,
                            sourceCompatibility = baseAppModuleExtension.compileOptions.sourceCompatibility.toString(),
                            targetCompatibility = baseAppModuleExtension.compileOptions.targetCompatibility.toString(),
                        ).apply {
                            println("$TAG, AspectjConfig = $this")
                        }
                    )

                val name = "${variant.name}Aspectj"
                val taskProvider = project.tasks.register<AspectjTask>(name) {
                    group = "aspectj"
                    description = name
                    bootClasspath.set(androidComponents.sdkComponents.bootClasspath)
                    classpath = variant.compileClasspath
                }

                variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                    .use(taskProvider)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        AspectjTask::allJars,
                        AspectjTask::allDirectories,
                        AspectjTask::outputFile
                    )
            }

        }
    }

}