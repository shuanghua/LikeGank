plugins {
    `kotlin-dsl`
}

group = "dev.shuanghua.android.plugin"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_17.toString().toInt())
}

dependencies {
//    implementation gradleApi()
//
//    implementation localGroovy()

    implementation ("com.android.tools.build:gradle:8.2.2")

    implementation("commons-io:commons-io:2.16.1")
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")

    implementation("com.squareup:kotlinpoet:1.16.0")
}


gradlePlugin {
    plugins {
        register("nav-plugin") { // Library
            id = "nav-plugin"
            implementationClass = "NavPlugin"
        }
    }
}
