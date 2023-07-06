import org.apache.commons.io.FileUtils
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

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    implementation("com.formdev:flatlaf:2.5")
    implementation("com.formdev:flatlaf-extras:2.5")
    implementation("com.formdev:flatlaf-intellij-themes:2.5")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

fun log(any: String) {
    println("builder: $any")
}


tasks.register("copyWeb") {
//    val target = File(buildDir, "resources/main/static")
//    target.mkdirs()
//    doLast {
//        val from = rootProject.file("dist")
//        if (from.exists()) {
//            println("copy $from to $target")
//            from.copyRecursively(target, true)
//        }
//    }
}

tasks.processResources {
    dependsOn("copyWeb")
}

tasks.bootJar {
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

val buildNativeHome: Directory by lazy {
    val app = compose.desktop.application
    app.nativeDistributions.outputBaseDir
        .dir("main/${TargetFormat.AppImage.outputDirName}").get()
}

enum class OS(val id: String) {
    Linux("linux"), Windows("windows"), MacOS("macos")
}

val currentOS: OS by lazy {
    val os = System.getProperty("os.name")
    when {
        os.equals("Mac OS X", ignoreCase = true) -> OS.MacOS
        os.startsWith("Win", ignoreCase = true) -> OS.Windows
        os.startsWith("Linux", ignoreCase = true) -> OS.Linux
        else -> error("Unknown OS name: $os")
    }
}

tasks.register("buildWithJBR") {
    dependsOn("createDistributable")
    group = "build"
    doLast {
        val app = compose.desktop.application
        val runtime = File(System.getProperty("java.home")).parentFile

        val directory = if (currentOS == OS.MacOS) {
            buildNativeHome.dir("${app.nativeDistributions.packageName}.app/Contents/runtime/Contents")
        } else {
            buildNativeHome.dir("${app.nativeDistributions.packageName}/runtime")
        }
        log(runtime.canonicalPath)
        log(directory.asFile.canonicalPath)
        FileUtils.deleteDirectory(directory.asFile)

        if (currentOS == OS.MacOS) {
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


compose.desktop {

    application {
        disableDefaultConfiguration()
        mainClass = "org.springframework.boot.loader.JarLauncher"
        val bootJar = tasks.bootJar.get().archiveFile
        fromFiles(bootJar)
        mainJar.set(bootJar)
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "compose-spring-boot"
            packageVersion = "1.0.0"
        }
    }
}
