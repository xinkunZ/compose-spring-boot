# Compose Spring boot

`Compose Spring boot` is a template project for building a GUI client with kotlin compose and spring boot to solve this problem: 
* build the java backend together with the web pages into a single GUI client
* the web page required Chromium core which java swing does not support

## What this project contains or not 
1. kotlin dsl gradle build script for kotlin compose desktop, but no mobile support 
2. use Jetbrains runtime 17 to build this project because it has full support for jcef
3. because kotlin compose build native installer with `jpackage`, and `jcef` is not a standard java module, 
so there is a gradle task to copy jetbrains runtime to native distribution, then it can not create Msi on windows, but macOS works well with Dmg



