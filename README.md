### 依赖

添加 jitpack 仓库

maven { url 'https://jitpack.io' }

效[果如下：
```
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
		    ...
			maven { url 'https://jitpack.io' }
			...
		}
	}
```]()


依赖 Aspectj 的运行时的包
```
api "org.aspectj:aspectjrt:1.9.21"
```

最后依赖 Aspectj 的 Gradle 插件. 让项目中所有切面生效

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

### 参数配置

```Groovy
aspectjConfig {
    // 默认是开启的
    enable = true
    // 默认是关闭的
    enableAspectLog = false
    // 默认不开启高级匹配
    // 当开启高级匹配时, includePackagePrefixSet 和 excludePackagePrefixSet 就不生效了
    // 仅支持 * 和 ** 两种通配符, * 表示匹配一个任意的名称, ** 表示匹配任意多个名称
    enableAdvancedMatch = false
    // 目标包名的前缀, 会匹配到这个包名下的所有类 enableAdvancedMatch = false 时生效
    includePackagePrefixSet = [
        "com.xiaojinzi",
    ]
    // 排除的包名的前缀, 会排除掉这个包名下的所有类 enableAdvancedMatch = false 时生效
    excludePackagePrefixSet = [
        "com.xiaojinzi.test",
    ]
    // 目标包名的匹配表达式, 会匹配到这个包名下的所有类 enableAdvancedMatch = true 时生效
    includePackagePatternSet = [
        "com.xiaojinzi.**",
    ]
    // 排除的包名的匹配表达式, 会排除掉这个包名下的所有类 enableAdvancedMatch = true 时生效
    excludePackagePatternSet = [
        "com.xiaojinzi.test.**",
    ]
}
```

### 使用 Aspectj 的能力

这个就得自己参看 Aspectj 的语法, 去写自己的切面了
