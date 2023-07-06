import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm")
    kotlin("kapt")
    id("io.spring.dependency-management") version "1.1.0"
    id("org.springframework.boot") version "3.1.0" apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
}

val mavenRelease: String? by project
val mavenSnapshot: String? by project

val springCloudVersion = "2022.0.2"

buildscript {
    dependencies {
        classpath("commons-io:commons-io:2.11.0")
    }

    repositories {
        maven("http://maven.aliyun.com/nexus/content/groups/public") {
            isAllowInsecureProtocol = true
        }
    }
}

allprojects {
    group = "com.kc.phoenix"
    version = "1.0-SNAPSHOT"

    repositories {
        maven("https://maven.aliyun.com/repository/central")
        maven("http://maven.aliyun.com/nexus/content/groups/public") {
            isAllowInsecureProtocol = true
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        if (!mavenSnapshot.isNullOrBlank()) {
            maven(mavenSnapshot!!) { isAllowInsecureProtocol = true }
        }
        if (!mavenRelease.isNullOrBlank()) {
            maven(mavenRelease!!) { isAllowInsecureProtocol = true }
        }
        mavenCentral()
    }

}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("kotlin-kapt")
        plugin("kotlin-spring")
        plugin("io.spring.dependency-management")
    }

    java.sourceCompatibility = JavaVersion.VERSION_17

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
        dependencies {
            dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
            dependency("com.github.binarywang:weixin-java-mp:4.5.0")
            dependency("com.alibaba:druid-spring-boot-starter:1.2.8")
            dependency("com.aliyun.oss:aliyun-sdk-oss:3.8.0")
            dependency("com.aventrix.jnanoid:jnanoid:2.0.0")
            dependency("org.apache.derby:derby:10.16.1.1")
        }
    }

    dependencies {
        implementation("com.google.guava:guava:31.1-jre")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        kapt("org.springframework.boot:spring-boot-configuration-processor")
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    publishing {
        if (mavenSnapshot.isNullOrBlank() || mavenRelease.isNullOrBlank()) {
            return@publishing
        }
        repositories {
            maven(uri(if (version.toString().endsWith("SNAPSHOT")) mavenSnapshot!! else mavenRelease!!))
        }
        val bootNames = setOf("compose-spring-boot-app")

        if (!bootNames.contains(project.name)) {
            publishing {
                publications {
                    create<MavenPublication>("mavenJava") {
                        artifact(tasks.jar)
                    }
                }
            }
        }

        project.pluginManager.withPlugin("org.springframework.boot") {
            project.publishing {
                publishing {
                    publications {
                        create<MavenPublication>("bootJava") {
                            artifact(tasks.named("bootJar"))
                        }
                    }
                }
            }
        }
    }

    afterEvaluate {
        tasks.withType(PublishToMavenRepository::class.java) {
            dependsOn(rootProject.tasks.build)
        }
    }

}




