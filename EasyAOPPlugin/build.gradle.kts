import com.builder.ModelTable
import com.builder.versions.DepTable

//buildscript {
//    repositories {
//        maven(url = "https://plugins.gradle.org/m2/")
//        mavenCentral()
//        mavenLocal()
//        google()
//        gradlePluginPortal()
//    }
//    dependencies {
//        classpath(DepTable.Build.gradle)
//        classpath(DepTable.Build.builder)
//        classpath(DepTable.Kotlin.kotlin_gradle_plugin)
//    }
//}

plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("com.gradle.plugin-publish") version "0.20.0"
//    id("maven-publish")
}

repositories {
    maven(url = "https://plugins.gradle.org/m2/")
    mavenCentral()
    mavenLocal()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(ModelTable.easyAopCfg)
    implementation(ModelTable.easyTools)
    implementation(ModelTable.easyAopAsm)
    implementation(DepTable.Build.gradle_api)
    implementation(DepTable.Build.gradle)
    compileOnly(DepTable.Build.builder4)  //先用4，解决3.4.x版本编译器报错问题
    compileOnly(DepTable.Build.builder_model)
    implementation(DepTable.Kotlin.kotlin_stdlib_jdk8)
    implementation(DepTable.Kotlin.kotlin_reflect)
    implementation(DepTable.apache_commons_io)
    implementation(DepTable.apache_commons_compress)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
//
//// The project version will be used as your plugin version when publishing
//version = "0.1.0"
//group = "com.wuba.plugin"

//val groupId = property("groupId") as String
val artifactId = property("artifactId") as String

// Use java-gradle-plugin to generate plugin descriptors and specify plugin ids
gradlePlugin {
    plugins {
        create(artifactId) {
            id = "com.wuba.plugin.easyaop"
            implementationClass = "com.wuba.plugin.easyaop.EasyAOPPlugin"
        }
    }
}

//https://plugins.gradle.org/plugin/com.gradle.plugin-publish
//https://plugins.gradle.org/docs/publish-plugin
pluginBundle {
    // These settings are set for the whole plugin bundle
    website = "https://github.com/wuba/EasyAOP"
    vcsUrl = "https://github.com/wuba/EasyAOP"

    // tags and description can be set for the whole bundle here, but can also
    // be set / overridden in the config for specific plugins
    description =
        "A simple and easy-to-use gradle plugin that implements aspect capabilities on Android through configuration"

    // Plugin config blocks can set the displayName, description and tags for
    // each plugin. displayName is mandatory. If no tags or description are set
    // the tags or description from the pluginBundle block will be used,
    // but they must be set in one of the two places.

    (plugins) {
        artifactId {
            // id is captured from java-gradle-plugin configuration
            displayName = "Android Gradle EasyAOP Plugin"
            description =
                "A simple and easy-to-use gradle plugin that implements aspect capabilities on Android through configuration"
            tags = listOf("ASM", "AOP", "aspect", "config", "yaml", "Android", "EasyAOP")
        }

    }
}