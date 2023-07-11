package com.kc.phoenix.studio

import java.io.IOException
import java.net.Socket

lateinit var systemUrl: String

private fun isLocalPortInUse(port: Int): Boolean {
    try {
        Socket("localhost", port).use { ignored -> return true }
    } catch (ignored: IOException) {
        return false
    }
}

// use random port to avoid port conflict
fun withPort(args: Array<String>): List<String> {
    var port = 9490
    while (port < 9999) {
        if (isLocalPortInUse(port)) {
            MAIN_LOGGER.warn("$port port is in use, try to find another port")
        } else {
            break
        }
        port++
    }
    val newArgs = arrayListOf(*args)
    newArgs.removeIf { it.startsWith("--server.port=") }
    newArgs.add("--server.port=$port")
    MAIN_LOGGER.info("start with port {}", port)
    systemUrl = "http://localhost:$port/doc"
    return newArgs
}
