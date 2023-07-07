import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.springframework.boot")
    id("org.jetbrains.compose")
}


dependencies {
    implementation(project(":compose-spring-boot-api"))
    implementation(project(":compose-spring-boot-core"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(compose.desktop.currentOs) {
        exclude("org.jetbrains.compose.material")
    }
    implementation("com.bybutter.compose:compose-jetbrains-expui-theme:2.1.0")
    implementation(compose.material3)
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

fun log(any: String) {
    println("builder: $any")
}


tasks.register("copyWeb") {
    // put your copy web page logic, maybe copy from node dist directory
}

tasks.processResources {
    dependsOn("copyWeb")
}

tasks.bootJar {
    // do not use .jar to prevent compose gradle plugin unzip it
    archiveFileName.set("main.boot")
}

tasks.register("installer") {
    group = "build"
}

tasks.whenTaskAdded {
    if (this.name == "createDistributable") {
        this.dependsOn(tasks.build)
    }
    if (this.name == "packageDistributionForCurrentOS") {
        tasks["installer"].dependsOn(this)
    }
}

compose.desktop {

    application {
        disableDefaultConfiguration()
        // use spring boot launcher
        mainClass = "org.springframework.boot.loader.JarLauncher"
        val bootJar = tasks.bootJar.get().archiveFile
        configurations.runtimeClasspath.get().forEach {
            if (it.name.contains("skiko-awt-runtime-")) {
                fromFiles(it)
            }
        }
        fromFiles(bootJar)
        mainJar.set(bootJar)
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("jdk.unsupported", "jcef")
            includeAllModules = true
            packageName = "compose-spring-boot"
            packageVersion = "1.0.0"
        }
    }
}
