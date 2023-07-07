package com.kc.phoenix.studio.app

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.web.bind.annotation.RestController

/**
 * @author zhangxinkun
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@RestController
class StudioApplication


val MAIN_LOGGER = LoggerFactory.getLogger(StudioApplication::class.java)

fun main(args: Array<String>) {
    startWithUI(args)
}


