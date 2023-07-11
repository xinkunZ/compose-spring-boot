package com.kc.phoenix.studio;

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

/**
 * @author zhangxinkun
 */
@Controller
class Beans {

    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("map-studio")
            .pathsToMatch("/**")
            .packagesToScan("com.kc.phoenix")
            .build();
    }

    @GetMapping("/doc")
    fun swagger(): String {
        return "redirect:swagger-ui/index.html"
    }

}
