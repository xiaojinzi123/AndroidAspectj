### 依赖

首先依赖 Aspectj 的运行时
```
api "org.aspectj:aspectjrt:1.9.21"
```

下面的 <version> 请替换为这里的具体版本：[![](https://jitpack.io/v/xiaojinzi123/AndroidAspectj.svg)](https://jitpack.io/#xiaojinzi123/AndroidAspectj)

在项目根目录依赖 Aspectj 的 Gradle 插件

```
classpath "com.github.xiaojinzi123.AndroidAspectj:aspectj-plugin:<version>"
```

然后在 Application 模块使用这个插件

```
plugins {
    id 'com.android.application'
    id 'com.xiaojinzi.aspectj.plugin'
    ......
}
```

![image](https://github.com/xiaojinzi123/AndroidAspectj/assets/12975743/36255505-8847-4a11-89da-45393987686b)

### 使用 Aspectj 的能力

这个就得自己参看 Aspectj 的语法, 去写自己的切面了
