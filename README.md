# LikeGank [MVP]

本项目基于最新 AndroidStudio 正式版更新

## 使用的开源库：
- database：[Realm](https://github.com/realm/realm-java "Realm")
- Async：[Rxjava](https://github.com/ReactiveX/RxJava "Rxjava")
- network: [Retrofit2](https://github.com/square/retrofit "Retrofit2")
- recyclerview multi type：[Multiype](https://github.com/drakeet/MultiType "Multiype")
- photo view：[Photoview](https://github.com/chrisbanes/PhotoView "Photoview")
- image download：[Glide](https://github.com/bumptech/glide "Glide")
- view bind：[ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- log: [Timber](https://github.com/JakeWharton/timber)
- permissions：[RxPermissions](https://github.com/tbruyelle/RxPermissions)

[GooglePlay下载](https://play.google.com/store/apps/details?id=com.shua.likegank "GooglePlay") 

<img src="https://github.com/shuanghua/LikeGank/blob/master/art/1.png" alt="Cover" width="25%"/> <img src="https://github.com/shuanghua/LikeGank/blob/master/art/2.png" alt="Cover" width="25%"/> <img src="https://github.com/shuanghua/LikeGank/blob/master/art/3.png" alt="Cover" width="25%"/> <img src="https://github.com/shuanghua/LikeGank/blob/master/art/4.png" alt="Cover" width="25%"/> 

# 项目模块介绍
## nav-annotation
放公共注解代码，也可以发布到仓库的库(先发布到仓库，否则其它模块可能丢失依赖)

## nav-compiler
放 kapt 相关的代码, 是一个可以发布到仓库的库(依赖 nav-annotation)

## build-logic
放 gradle plugin 相关的代码，是一个可以发布到仓库的 gradle 插件

build-logic 其实可以当成一个单独的项目，并不依附于 LikeGank 项目

当编译 LikeGank 时，通过 LikeGank settings.gradle 中的 includeBuild("build-logic") 来编译 build-logic，
这意味这 build-logic 是可以放在其它目录下的

build-logic 想使用 LikeGank gradle 目录下的 libs.versions.toml ，
则需要在自己的 settings.gradle.kts 中引入 toml
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

# 开源协议
LikeGnk 基于 GPL-3.0 开源协议，在使用本项目代码的之前请确保你了解这个协议。 协议详细内容参见 [GPL-3.0 LICENSE](https://github.com/Shuanghua/LikeGank/blob/master/LICENSE "GPL-3.0 LICENSE")
