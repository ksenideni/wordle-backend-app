package ru.mirea.wordle.config.telegram

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegram")
data class TelegramProperties (
        val host: String,
        val token: String,
)