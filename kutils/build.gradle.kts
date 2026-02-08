plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.3.0"
    `maven-publish`
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("com.github.ArtLoz.Kbridge:bridge-api-models:1.0.4")
                implementation("com.github.ArtLoz.Kbridge:bridge-api-core:1.0.4")
                implementation("org.xerial:sqlite-jdbc:3.45.1.0")
            }
        }
    }
}

group = libs.versions.sdkGroup.get()
version = libs.versions.sdkVersion.get()