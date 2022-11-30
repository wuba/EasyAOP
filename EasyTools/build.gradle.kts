plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
dependencies {
    implementation(com.builder.versions.DepTable.Kotlin.kotlin_stdlib_jdk8)
    implementation(com.builder.versions.DepTable.Kotlin.kotlin_reflect)
    implementation(com.builder.versions.DepTable.squareup_okio)
}