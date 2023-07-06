package com.kc.phoenix.studio.app

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

fun main(args: Array<String>) {
    startWithUI(args)
}


