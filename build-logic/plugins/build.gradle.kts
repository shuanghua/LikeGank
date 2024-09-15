plugins {
     id("java-gradle-plugin")
     alias(libs.plugins.kotlin.jvm)
//    id ("java-gradle-plugin") // 开发本地插件
//    id("com.gradle.plugin-publish") version "1.2.1" // 开发远程插件, 只有添加了该插件才会出现 plugin_portal 脚本
//    id ("maven-publish") // 开发三方库   发布库到本地或远程 Maven 仓库
}


group = "dev.shuanghua.android.plugin"


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


dependencies {
    compileOnly(libs.android.gradlePlugin.api) // 编译时依赖
    // 处理字节码相关库
    compileOnly(libs.commons.io)
    compileOnly(libs.asm)
    compileOnly(libs.asm.tree)
}





/**
 *  创建一个仅供"本地"使用插件
 *
 *  需要引入插件 id 'java-gradle-plugin'
 */
gradlePlugin {
    plugins {
        register("AsmBytecode") { // Library
            id = "dev.shuanghua.plugin.asmbytecode"
            version = "1.0.0"
            // 插件类 (kts + kt 或者 gradle + java , 千万不要 kts + java 或者 gradle + kt混用)
            implementationClass = "ModifyClassesPlugin"
        }
        // register 注册更多的插件
    }
}


/**
 *  发布一个"插件"到 Gradle Plugin Portal "远程"插件仓库
 *
 *  https://plugins.gradle.org/
 *  发布到 Gradle Plugin Portal 官方插件仓库一定要先将 api key 加入到本地 gradle.properties 文件中
 *  否则会报 401 错误。
 *
 *  需要引入插件: id("com.gradle.plugin-publish") version "1.2.1"
 * */
//gradlePlugin {
//    plugins {
//        create("xxxxx-plguin") {
//            id = "io.github.shuanghua.xxxxx-plguin"
//            implementationClass = "io.github.shuanghua.gradle.XxxxxPlguin"
//            displayName = "Gradle Xxxxx plugin"
//            description = "Xxxxx plugin for Gradle"
//            tags.set(listOf("search", "tags", "for", "your", "hello", "plugin")) // 更改标签
//        }
//    }
//}


/**
 *  发布一个 "库 "到本地 Maven Central 仓库
 *
 *  需要引入插件: id 'maven-publish'
 */
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
//}


/**
 * 发布一个"库"到远程 Maven Central 仓库
 *
 * 需要引入插件: id 'maven-publish'
 */
//publishing {
//    repositories { // 远程 Maven 仓库
//        maven {
//            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
//            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
//            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
//        }
//    }
//}

