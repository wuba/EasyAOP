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
    implementation(DepTable.jackson_databind)
    implementation(DepTable.jackson_dataformat_yaml)
    implementation(DepTable.apache_commons_text)
    implementation(ModelTable.easyTools)
}