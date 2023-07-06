# Compose Spring boot

`Compose Spring boot` is a template project for building a GUI client with kotlin compose and spring boot to solve these problem: 
* Electron can not fit your technology stack, you want use jvm platform
* You want build java backend together with the web pages into a single GUI client, which can use between multiplatform
* Your web page required Chromium core which java swing does not support


## What this project contains or not 
1. Kotlin dsl gradle build script for kotlin compose desktop, but no mobile support 
2. Use Jetbrains runtime 17 to build this project because it has full support for jcef
3. Because of kotlin compose build native installer with `jpackage`, and `jcef` is not a standard java module, 
there is a gradle task to copy jetbrains runtime to native distribution, then it can not create Msi on windows, but macOS works well with Dmg
4. The main jar use spring boot format, all classpath in `BOOT-INF/lib` instead of fat jar or copy all depenedencies into app folder for `jpackage`
5. You can fully use kotlin compose to write GUI if you do not use web page. If not, kotlin compose just be like a jvm-electron container
6. Can not reduce the native installer disk size(500MB+). In theory it contains JDK 17 and Chrome, bigger than Electron


