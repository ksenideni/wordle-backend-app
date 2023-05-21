package ru.mirea.wordle.config.telegram

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class TelegramConfig(val telegramProperties: TelegramProperties) {

    @Bean
    fun webClient(): WebClient {
        return WebClient.create("${telegramProperties.host}${telegramProperties.token}")
    }

}