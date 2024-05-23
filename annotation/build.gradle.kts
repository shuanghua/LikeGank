plugins {
    id("java-library")
    id ("maven-publish") // 发布   发布库到 Maven 仓库
    id("org.jetbrains.kotlin.jvm") apply true
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

/**
 * 发布本地 Maven 仓库
 */
publishing {
    publications {
        create<MavenPublication>("LikeGankAnnotation") {
            artifactId = "lib-annotation"
            from(components["java"])
            groupId = "dev.shuanghua.library"
            version = "1.0.0"
        }
    }
}