package com.kc.phoenix.studio.app

import org.slf4j.LoggerFactory
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.*
import org.springframework.context.support.beans
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

/**
 * @author zhangxinkun
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@RestController
class StudioApplication


val MAIN_LOGGER = LoggerFactory.getLogger(StudioApplication::class.java)

fun main(args: Array<String>) {
    MAIN_LOGGER.info("load spring boot")
    SpringApplicationBuilder(StudioApplication::class.java)
        .headless(false)
        .listeners(ApplicationReadyListener)
        .initializers(Beans)
        .run(*withPort(args).toTypedArray())
}

object Beans : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        beans {
            bean<GroupedOpenApi> {
                GroupedOpenApi.builder()
                    .group("spring-boot")
                    .pathsToMatch("/**")
                    .packagesToScan("com.kc.phoenix")
                    .build();
            }
        }
    }

    @Controller
    object HomeView {

        @GetMapping("/doc")
        fun swagger(): String {
            return "redirect:swagger-ui/index.html"
        }
    }


}

