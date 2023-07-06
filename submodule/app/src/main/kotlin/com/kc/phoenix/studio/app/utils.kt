package com.kc.phoenix.studio.app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.Socket

val log: Logger = LoggerFactory.getLogger("com.kc.phoenix.studio.app.start")

lateinit var systemUrl: String

private fun isLocalPortInUse(port: Int): Boolean {
    try {
        Socket("localhost", port).use { ignored -> return true }
    } catch (ignored: IOException) {
        return false
    }
}

fun withPort(args: Array<String>): List<String> {
    var port = 9490
    while (port < 9999) {
        if (isLocalPortInUse(port)) {
            log.warn("$port port is in use, try to find another port")
        } else {
            break
        }
        port++
    }
    val newArgs = arrayListOf(*args)
    newArgs.removeIf { it.startsWith("--server.port=") }
    newArgs.add("--server.port=$port")
    log.info("start with port {}", port)
    systemUrl = "http://localhost:$port"
    return newArgs
}
