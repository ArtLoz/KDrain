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

version = "1.1.0"
group = libs.versions.sdkGroup.get()

tasks.register<Copy>("buildPlugin") {
    group = "kdrain"
    description = "Build plugin JAR and copy to app/plugins"
    dependsOn(tasks.jar)
    from(tasks.jar.map { it.archiveFile })
    into("${rootProject.projectDir}/app/plugins/")
}
