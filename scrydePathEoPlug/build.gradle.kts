plugins {
    id("java-library")
    kotlin("jvm") version "2.3.0"
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

dependencies{
    compileOnly(project(":kutils"))
    compileOnly(project(":KDrainPluginApi"))
}

tasks.jar {
    doLast {
        copy {
            from(archiveFile)
            into("${rootProject.projectDir}/app/plugins/")
        }
    }
}