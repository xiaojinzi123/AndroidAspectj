# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn org.reactivestreams.FlowAdapters
-dontwarn org.reactivestreams.**
-dontwarn java.util.concurrent.flow.**
-dontwarn java.util.concurrent.**

# 小金子组件化框架 不要警告
-dontwarn com.xiaojinzi.aspectj.**
# 所有本包下面的类和接口都不混淆
-keep class com.xiaojinzi.aspectj.** {*;}
-keep interface com.xiaojinzi.aspectj.** {*;}
#这两条是让路由 Api 不混淆
-keep @com.xiaojinzi.aspectj.anno.router.RouterApiAnno interface * {*;}
-keep class **.**RouterApiGenerated {*;}
#几个用户自定义或者自动生成到其他包下的应该不混淆
-keep class * implements com.xiaojinzi.aspectj.impl.RouterInterceptor{*;}
-keep class * implements com.xiaojinzi.aspectj.support.IBaseLifecycle{*;}
-keep class * implements com.xiaojinzi.aspectj.application.IApplicationLifecycle{*;}
-keep class * implements com.xiaojinzi.aspectj.application.IComponentApplication{*;}
-keep class * implements com.xiaojinzi.aspectj.support.Inject{*;}