pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val composeVersion = extra["compose.version"] as String
        kotlin("jvm").version(kotlinVersion)
        id("org.jetbrains.compose").version(composeVersion)
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        id("io.spring.dependency-management") version "1.1.0"
        id("org.springframework.boot") version "3.1.0"
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
    }
}


rootProject.name = "compose-spring-boot"

include("compose-spring-boot-api")
include("compose-spring-boot-app")
include("compose-spring-boot-core")

rootProject.children.forEach {
    childrenName(it)
}

fun childrenName(project: ProjectDescriptor) {
    if (project.children.isNotEmpty()) {
        project.children.forEach { childrenName(it) }
    }
    val name = project.name.substringAfter("${rootProject.name}-")
    project.projectDir = File(rootDir, "submodule/$name")

    project.buildFileName = "$name.gradle.kts"
    println(project.name + " " + project.projectDir + "   ${project.buildFile}")
    if (!project.buildFile.exists()) {
        project.buildFile.parentFile.mkdirs()
        project.buildFile.createNewFile()
    }
}