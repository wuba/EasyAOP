buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.3")
        classpath("com.android.tools.build:builder:3.4.3")
        classpath("org.codehaus.groovy:groovy-all:3.0.7")
//        classpath(gradleApi())
//        classpath(localGroovy())
    }
}

plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    compileOnly("org.codehaus.groovy:groovy-all:3.0.7")
    implementation("com.squareup:javapoet:1.13.0")
//    compileOnly("com.google.code.gson:gson:2.8.9")
//    compileOnly("com.android.tools.build:builder:4.0.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets.main {
    java.srcDirs(
        "src/main/java",
        "src/main/kotlin"
//        "../EasyAOPPlugin/src/main/java"
    )
}

//// Use java-gradle-plugin to generate plugin descriptors and specify plugin ids
//gradlePlugin {
//    plugins {
//        easyAOPPlugin {
//            id = 'com.wuba.plugin.easyaop'
//            implementationClass = 'com.wuba.plugin.easyaop.EasyAOPPlugin'
//        }
//    }
//}
