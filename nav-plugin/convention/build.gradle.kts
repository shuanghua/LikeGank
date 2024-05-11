// Gradle Plugin Portal 官方插件仓库: 一般用于发布插件类型,一旦发布成功,就可以通过 id 直接引用插件。
// Maven Central 官方仓库: 一般用于发布库类型, 发布成功后, 其他项目通过 implementation 直接引用。

// 如果把插件发布到 Maven Central 仓库, 那么就需要先在项目更目录通过 classpath 方式引入插件。
// 然后在子模块的 build.gradle 文件中通过 apply plugin: 'xxx-xxx' 方式引用插件。
// [ 不推荐将插件类型的项目发布到 Maven Central 仓库 ]
plugins {
    `kotlin-dsl` // 已经包括了 Kotlin 插件 和 `java-gradle-plugin`
//    id ("java-gradle-plugin") // 开发   本地使用 Gradle Plugin, 上面已经包含了该插件
//    id("com.gradle.plugin-publish") version "1.2.1" // 发布, 只有添加了该插件才会出现 plugin_portal 脚本
//    id ("maven-publish") // 发布   发布库到 Maven 仓库
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


kotlin {
    jvmToolchain(JavaVersion.VERSION_17.toString().toInt())
}


dependencies {
    implementation("com.android.tools.build:gradle:8.2.2")

    // 处理字节码库
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("com.squareup:kotlinpoet:1.16.0")
}


group = "dev.shuanghua.android.plugin"
version = "1.0.0"


//gradlePlugin {
//    plugins {
//        register("nav-plugin") {
//            id = "nav-plugin"
//            implementationClass = "NavPlugin"
//        }
//    }
//}


// https://plugins.gradle.org/
// 发布到 Gradle Plugin Portal 官方插件仓库一定要先将 api key 加入到本地 gradle.properties 文件中
// 否则会报 401 错误。

// 如果是插件则使用 id 'java-gradle-plugin' + gradlePlugin{},
// 如果是库则使用 id 'maven-publish' + publishing{}
gradlePlugin {
    website = "https://github.com/shuanghua/xxxxxx"
    vcsUrl = "https://github.com/shuanghua/xxxxxx"
    plugins {
        register("nav-plugin") { // Library
            id = "nav-plugin"
            // 插件类 (kts + kt 或者 gradle + java , 千万不要 kts + java 或者 gradle + kt混用)
            implementationClass = "NavPlugin"
        }

        create("xxxxx-plguin") {
            id = "io.github.shuanghua.xxxxx-plguin"
            implementationClass = "io.github.shuanghua.gradle.XxxxxPlguin"
            displayName = "Gradle Xxxxx plugin"
            description = "Xxxxx plugin for Gradle"
            tags.set(listOf("search", "tags", "for", "your", "hello", "plugin")) // 更改标签
        }
    }
}

// 发布一个库
//publishing {
//    publications { // 本地 Maven 仓库
//        create<MavenPublication>("mavenJava") {
//            artifactId = "my-library"
//            from(components["java"])
//            versionMapping {
//                usage("java-api") {
//                    fromResolutionOf("runtimeClasspath")
//                }
//                usage("java-runtime") {
//                    fromResolutionResult()
//                }
//            }
//        }
//    }

//    repositories { // 远程 Maven 仓库
//        maven {
//            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
//            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
//            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
//        }
//    }
//}
