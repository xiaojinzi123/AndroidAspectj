package com.xiaojinzi.aspectj.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.joom.grip.Grip
import com.joom.grip.GripFactory
import com.joom.grip.annotatedWith
import com.joom.grip.classes
import com.xiaojinzi.aspectj.api.anno.AspectWeaveIgnore
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
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.register
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
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

        private val aspectjConfig = project.extra[EXT_ASPECTJ_CONFIG] as AspectjConfig

        // private val cacheFolder = File(project.buildDir, "aspectj")
        private val cacheFolder = File(
            System.getProperty("java.io.tmpdir"),
            "aspectjFolder${File.separatorChar}${project.rootProject.name}"
        )
        private val aspectClassFolder = File(cacheFolder, "aspect")
        private val aspectInputClassFolder = File(cacheFolder, "inputClass")
        private val aspectOutputClassFolder = File(cacheFolder, "outputClass")

        private val logger = project.logger

        private fun copyAspectClassFile(
            name: String,
            fileInputStream: InputStream,
        ) {
            val targetClassFile = File(aspectClassFolder, name)
            if (!targetClassFile.parentFile.exists()) {
                targetClassFile.parentFile.mkdirs()
            }
            targetClassFile.outputStream().use { outputStream ->
                fileInputStream.copyTo(outputStream)
            }
        }

        private fun copyBeAspectClassFile(
            name: String,
            fileInputStream: InputStream,
        ) {
            val targetClassFile = File(aspectInputClassFolder, name)
            if (!targetClassFile.parentFile.exists()) {
                targetClassFile.parentFile.mkdirs()
            }
            targetClassFile.outputStream().use { outputStream ->
                fileInputStream.copyTo(outputStream)
            }
        }

        @TaskAction
        fun taskAction() {

            val pathMatcher = PathMatcher(
                enableAdvancedMatch = aspectjConfig.enableAdvancedMatch,
                includePackagePrefixFormatSet = aspectjConfig.includePackagePrefixFormatSet,
                excludePackagePrefixFormatSet = aspectjConfig.excludePackagePrefixFormatSet,
                includePackagePatternFormatSet = aspectjConfig.includePackagePatternFormatSet,
                excludePackagePatternFormatSet = aspectjConfig.excludePackagePatternFormatSet,
            )

            println("${AspectjPlugin.TAG} aspectjConfig = $aspectjConfig")

            if (cacheFolder.exists() && !cacheFolder.deleteRecursively()) {
                throw RuntimeException("文件夹删除失败: ${cacheFolder.path}")
            }
            // cacheFolder.mkdirs()
            aspectClassFolder.mkdirs()
            aspectInputClassFolder.mkdirs()
            println("${AspectjPlugin.TAG} cacheFolder = ${cacheFolder.path}")
            println("${AspectjPlugin.TAG} allDirectories = ${allDirectories.get().joinToString()}")

            // /Users/hhkj/Documents/code/android/github/KComponent/Demo/app2/build/intermediates/classes/debug/ALL/classes.jar
            val outputFile = outputFile.asFile.get()
            // 可能里面有 outputFile 的 jar
            val allJarList = allJars.get()
            val targetAllJarList = allJarList.filter {
                it.asFile != outputFile
            }

            println("${AspectjPlugin.TAG} targetAllJarList = ${targetAllJarList.joinToString { it.asFile.path }}")
            println("${AspectjPlugin.TAG} outputFile = (exists: ${outputFile.exists()}) ,${outputFile.path}")

            // 输入的 jar、aar、源码
            val inputs: List<java.nio.file.Path> =
                (targetAllJarList + allDirectories.get()).map { it.asFile.toPath() }

            // 系统依赖
            val classPaths = bootClasspath.get().map { it.asFile.toPath() }
                .toSet() + classpath.files.map { it.toPath() }

            val grip: Grip =
                GripFactory.newInstance(Opcodes.ASM9)
                    .create(classPaths + inputs)

            // 先找到 Aspect 切面类, 然后复制到统一的一个文件夹
            val targetAspectClassNameSet = grip
                .select(classes)
                .from(inputs)
                .where(
                    annotatedWith(
                        annotationType = com.joom.grip.mirrors.getType(
                            descriptor = "Lorg/aspectj/lang/annotation/Aspect;",
                        )
                    )
                )
                .execute()
                .classes
                .map { classMirror ->
                    "${
                        classMirror.name.replace(
                            oldChar = '.', newChar = File.separatorChar,
                        )
                    }.class"
                }
                .toSet()

            // 找到需要忽略的所有 class 的前缀
            val targetAspectIgnoreClassPrefixNameSet = grip
                .select(classes)
                .from(inputs)
                .where(
                    annotatedWith(
                        annotationType = com.joom.grip.mirrors.getType(
                            descriptor = "L${
                                AspectWeaveIgnore::class.java.name.replace(
                                    oldValue = ".",
                                    newValue = "/",
                                )
                            };",
                        )
                    )
                )
                .execute()
                .classes
                .map { classMirror ->
                    classMirror.name.replace(
                        oldChar = '.', newChar = File.separatorChar,
                    )
                }
                .toSet()

            println("${AspectjPlugin.TAG} targetAspectClassNameSet = ${targetAspectClassNameSet.joinToString()}")

            allDirectories
                .get()
                .apply {
                    println("${AspectjPlugin.TAG} directory.size = ${this.size}")
                }
                .forEach { directory ->
                    println("${AspectjPlugin.TAG} ${directory.asFile.path}")
                }

            val jarOutput = JarOutputStream(
                BufferedOutputStream(
                    FileOutputStream(
                        outputFile
                    )
                )
            )

            targetAllJarList.forEach { file ->
                val jarFile = JarFile(file.asFile)
                jarFile.entries().iterator().forEach { jarEntry ->
                    // jarEntry.name
                    // com/google/zxing/qrcode/decoder/Mode.class
                    // xxxxxxxxxxxxxx.class
                    // 切面的类都复制出来
                    if (!jarEntry.isDirectory && targetAspectClassNameSet.contains(element = jarEntry.name)) {
                        jarFile.getInputStream(jarEntry)?.use {
                            copyAspectClassFile(
                                name = jarEntry.name,
                                fileInputStream = it,
                            )
                        }
                    }

                    val isMatch = !jarEntry.isDirectory && !targetAspectIgnoreClassPrefixNameSet
                        .any {
                            jarEntry.name.startsWith(prefix = it)
                        } && pathMatcher.isMatch(path = jarEntry.name)
                    // 如果匹配了
                    if (isMatch) {
                        jarFile.getInputStream(jarEntry).use {
                            copyBeAspectClassFile(
                                name = jarEntry.name,
                                fileInputStream = it,
                            )
                        }
                    } else {
                        try {
                            jarOutput.putNextEntry(
                                JarEntry(
                                    jarEntry.name
                                )
                            )
                            jarFile.getInputStream(jarEntry).use {
                                it.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
                jarFile.close()
            }

            allDirectories.get().forEach { directory ->
                directory.asFile.walk().forEach { file ->
                    if (file.isFile) {
                        // AppMxModifyFlagKt.class
                        // com/zm/mx/app_info/view/AppInfoAct.class
                        val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                        // 切面的类都复制出来
                        if (targetAspectClassNameSet.contains(element = relativePath)) {
                            file.inputStream().use {
                                copyAspectClassFile(
                                    name = relativePath,
                                    fileInputStream = it,
                                )
                            }
                        }
                        val isMatch =
                            !targetAspectIgnoreClassPrefixNameSet.any {
                                relativePath.startsWith(prefix = it)
                            } && pathMatcher.isMatch(
                                path = relativePath
                            )
                        if (isMatch) {
                            file.inputStream().use {
                                copyBeAspectClassFile(
                                    name = relativePath,
                                    fileInputStream = it,
                                )
                            }
                        } else {
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

            // 执行 aspect 编译器
            val aspectjCompiler = org.aspectj.tools.ajc.Main()
            val messageHandler = MessageHandler()
            aspectjCompiler.run(
                arrayOf(
                    "-d", aspectOutputClassFolder.path,
                    // "-inpath", "/Users/hhkj/Documents/code/java/temp/AspectjDemo1/Aspectj/build/libs/Aspectj-1.0.jar:/Users/hhkj/Documents/code/java/temp/AspectjDemo1/Jar/build/libs/Jar-1.0.jar",
                    "-inpath", aspectInputClassFolder.path,
                    "-aspectpath", aspectClassFolder.path,
                    "-target", aspectjConfig.targetCompatibility,
                    "-source", aspectjConfig.sourceCompatibility,
                    "-classpath",
                    // Jar 的所有 path 也加进来
                    (classpath.files + targetAllJarList.map { it.asFile }).joinToString(
                        separator = File.pathSeparator,
                    ).apply {
                        println("$TAG, classpath = $this")
                    },
                    "-bootclasspath",
                    bootClasspath
                        .get()
                        .joinToString(
                            separator = File.pathSeparator,
                        ).apply {
                            println("$TAG, bootclasspath = $this")
                        },
                ),
                messageHandler
            )

            println("${AspectjPlugin.TAG} aspectj message log start")
            messageHandler.getMessages(null, true).forEach {
                if (aspectjConfig.enableAspectLog) {
                    when (it.kind) {
                        IMessage.ERROR -> {
                            logger.error(it.message, it.thrown)
                            throw RuntimeException("aspectj weave failed")
                        }

                        IMessage.WARNING -> {
                            logger.warn(it.message, it.thrown)
                        }

                        IMessage.INFO -> {
                            logger.info(it.message, it.thrown)
                        }

                        IMessage.DEBUG -> {
                            logger.debug(it.message, it.thrown)
                        }
                    }
                }
            }
            println("${AspectjPlugin.TAG} aspectj message log end")

            aspectOutputClassFolder.walk().forEach { file ->
                if (file.isFile) {
                    val relativePath = aspectOutputClassFolder.toURI().relativize(file.toURI()).path
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

            jarOutput.close()

        }

    }

    override fun apply(project: Project) {

        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        // project.plugins.withType(AppPlugin::class.java)
        println("$TAG, isApp = $isApp, project = ${project.name}")

        if (!isApp) {
            return
        }

        // 添加扩展
        project.extensions.add("aspectjConfig", AspectjInitConfig::class.java)

        val androidComponents = project.extensions
            .findByType(AndroidComponentsExtension::class.java)

        androidComponents?.onVariants { variant ->

            val aspectjConfig = project.extensions.findByType(AspectjInitConfig::class.java)
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
                            enableAspectLog = aspectjConfig?.enableAspectLog ?: false,
                            enableAdvancedMatch = aspectjConfig?.enableAdvancedMatch ?: false,
                            sourceCompatibility = baseAppModuleExtension.compileOptions.sourceCompatibility.toString(),
                            targetCompatibility = baseAppModuleExtension.compileOptions.targetCompatibility.toString(),
                            includePackagePrefixSet = aspectjConfig?.includePackagePrefixSet
                                ?: emptySet(),
                            excludePackagePrefixSet = aspectjConfig?.excludePackagePrefixSet
                                ?: emptySet(),
                            includePackagePatternSet = aspectjConfig?.includePackagePatternSet
                                ?: emptySet(),
                            excludePackagePatternSet = aspectjConfig?.excludePackagePatternSet
                                ?: emptySet(),
                        )
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