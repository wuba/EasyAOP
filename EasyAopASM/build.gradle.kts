import com.builder.ModelTable
import com.builder.versions.DepTable

plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(DepTable.Kotlin.kotlin_stdlib_jdk8)
    implementation(DepTable.Kotlin.kotlin_reflect)
    implementation(ModelTable.easyAopCfg)
    implementation(ModelTable.easyTools)
    implementation(DepTable.asm)
    implementation(DepTable.asm_tree)
    implementation(DepTable.asm_util)
    implementation(DepTable.asm_analysis)
    implementation(DepTable.asm_commons)
}