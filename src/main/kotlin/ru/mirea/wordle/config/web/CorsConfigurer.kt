package ru.mirea.wordle.config.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfigurer(
    @param:Value(value = "\${endpoints.cors.allowed-origins}") var frontendUrl: String
) : WebMvcConfigurer {

    override fun addCorsMappings(
        registry: CorsRegistry,
    ) {
        super.addCorsMappings(registry)
        registry.addMapping("/**").allowedOrigins(frontendUrl)
    }
}