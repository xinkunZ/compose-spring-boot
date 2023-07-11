import org.apache.commons.io.FileUtils
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo

plugins {
    id("org.springframework.boot")
    id("org.jetbrains.compose")
}


dependencies {
    implementation(project(":compose-spring-boot-api"))
    implementation(project(":compose-spring-boot-core"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

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
    archiveFileName.set("main.boot")
}

val buildNativeHome: Directory by lazy {
    val app = compose.desktop.application
    app.nativeDistributions.outputBaseDir
        .dir("main/${TargetFormat.AppImage.outputDirName}").get()
}

tasks.register("buildWithJBR") {
    dependsOn("createDistributable")
    group = "build"
    doLast {
        val app = compose.desktop.application
        val runtime = File(System.getProperty("java.home")).parentFile

        val directory = if (SystemInfo.isMac) {
            buildNativeHome.dir("${app.nativeDistributions.packageName}.app/Contents/runtime/Contents")
        } else {
            buildNativeHome.dir("${app.nativeDistributions.packageName}/runtime")
        }
        log(runtime.canonicalPath)
        log(directory.asFile.canonicalPath)
        FileUtils.deleteDirectory(directory.asFile)

        if (SystemInfo.isMac) {
            FileUtils.deleteDirectory(directory.asFile)
            // macOS下默认复制文件是不带权限的，因此如果使用FileUtils复制进去的jdk是无法运行的(丢失了chmod 777)
            val args = arrayOf("cp", "-p", "-r", runtime.canonicalPath, directory.asFile.canonicalPath)
            log(args.joinToString(" "))
            Runtime.getRuntime().exec(args)
        } else {
            FileUtils.copyDirectory(runtime, directory.asFile)
        }
    }
}


tasks.register("releaseZip") {
    group = "build"
    dependsOn("buildWithJBR")
}

tasks.whenTaskAdded {
    if (this.name == "createDistributable") {
        this.dependsOn(tasks.build)
        tasks["buildWithJBR"].dependsOn(this)
    }
    if (this.name == "packageDistributionForCurrentOS") {
        this.dependsOn(tasks["buildWithJBR"])
        if (SystemInfo.isMac) {
            tasks["releaseZip"].dependsOn(this)
        }
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
            includeAllModules = true
            packageName = "compose-spring-boot"
            packageVersion = "1.0.0"
        }
    }
}
