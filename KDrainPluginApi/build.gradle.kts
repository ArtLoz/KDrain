plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    `maven-publish`
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }

}

group = libs.versions.sdkGroup.get()
version = libs.versions.sdkVersion.get()
dependencies {
    api("com.github.ArtLoz.Kbridge:bridge-api:1.0.4")
}
