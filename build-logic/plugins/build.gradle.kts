plugins {
    `java-gradle-plugin`
     alias(libs.plugins.kotlin.jvm)
//    id ("java-gradle-plugin") // 开发本地插件
//    id("com.gradle.plugin-publish") version "1.2.1" // 开发远程插件, 只有添加了该插件才会出现 plugin_portal 脚本
//    id ("maven-publish") // 开发三方库   发布库到本地或远程 Maven 仓库
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


dependencies {
    compileOnly(libs.android.gradlePlugin.api) // 编译时依赖

    // 处理字节码库
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("com.squareup:kotlinpoet:1.16.0")
    implementation("javassist:javassist:3.4.GA")

    implementation("dev.shuanghua.library:android-annotation:1.0.0")
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


/**
 *  创建一个仅供"本地"使用插件
 *
 *  需要引入插件 id 'java-gradle-plugin'
 */
gradlePlugin {
    plugins {
        register("nav-plugin") { // Library
            id = "nav-plugin"
            // 插件类 (kts + kt 或者 gradle + java , 千万不要 kts + java 或者 gradle + kt混用)
            implementationClass = "NavPlugin"
        }
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
 *  发布一个"库"到本地 Maven Central 仓库
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

