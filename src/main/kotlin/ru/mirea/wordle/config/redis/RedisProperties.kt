package ru.mirea.wordle.config.redis

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Класс, предназначенный для хранения конфигураций подкючения к Redis
 *
 * @param user имя пользователя
 * @param password пароль пользователя
 * @param host URL сервера Redis
 * @param port порт подключения к Redis
 *
 * */
@ConfigurationProperties(prefix = "redis")
data class RedisProperties(
    val user: String = "default",
    val password: String?,
    val host: String,
    val port: Int = 6379
)