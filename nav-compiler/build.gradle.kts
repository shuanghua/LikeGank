plugins {
    id("kotlin")
    id ("org.jetbrains.kotlin.kapt")
    `maven-publish`
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.16.0") // 用到反射则不能使用 compileOnly
    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
    //依赖 nav-annotation
    compileOnly(project(":nav-annotation"))
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kapt {
    arguments {
        arg("kapt.kotlin.generated", file("${buildDir}/generated/source/kaptKotlin"))
    }
}

publishing {
    publications {
        create<MavenPublication>("NavCompiler") {
            from(components["java"])
            groupId = "dev.shuanghua.library"
            artifactId = "nav-compiler-kapt"
            version = "1.0.0"
        }
    }
}