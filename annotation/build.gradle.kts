plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id ("maven-publish") // 发布   发布库到 Maven 仓库
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications { // 本地 aven 仓库
        create<MavenPublication>("LikeGankAnnotation") {
            artifactId = "android-annotation"
            from(components["java"])
            groupId = "dev.shuanghua.library"
            version = "1.0.0"
        }
    }
}