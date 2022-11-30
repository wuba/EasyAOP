package com.builder.versions;

/**
 * Created by wswenyue on 2022/1/17.
 */
public interface DepTable {
    String gson = "com.google.code.gson:gson:2.8.9";
    String jackson_databind = "com.fasterxml.jackson.core:jackson-databind:2.13.0";
    String jackson_dataformat_yaml = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0";
    String apache_commons_io = "commons-io:commons-io:2.7";
    String apache_commons_compress = "org.apache.commons:commons-compress:1.21";
    String apache_commons_text = "org.apache.commons:commons-text:1.9";
    String asm_version = "9.2";
    String asm = "org.ow2.asm:asm:" + asm_version;
    String asm_tree = "org.ow2.asm:asm-tree:" + asm_version;
    String asm_util = "org.ow2.asm:asm-util:" + asm_version;
    String asm_analysis = "org.ow2.asm:asm-analysis:" + asm_version;
    String asm_commons = "org.ow2.asm:asm-commons:" + asm_version;
    String squareup_okio = "com.squareup.okio:okio:3.2.0";

    interface Kotlin {
        String version = "1.6.21";
        String kotlin_gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:" + version;
        String kotlin_stdlib_jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + version;
        String kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:" + version;
    }

    interface Android {
        interface X {
            String core_ktx = "androidx.core:core-ktx:1.7.0";
            String appcompat = "androidx.appcompat:appcompat:1.0.0";
            String constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3";
        }

        String material = "com.google.android.material:material:1.0.0";
    }

    interface Gradle {
        String plugin_publish = "com.gradle.publish:plugin-publish-plugin:0.19.0";
    }

    interface Build {
        String version = "3.5.2";
        String gradle = "com.android.tools.build:gradle:" + version;
        String gradle_api = "com.android.tools.build:gradle-api:" + version;
        String builder = "com.android.tools.build:builder:" + version;
        String builder4 = "com.android.tools.build:builder:4.0.0";
        String builder_model = "com.android.tools.build:builder-model:" + version;
    }
}
