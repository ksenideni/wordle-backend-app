package ru.mirea.wordle.config.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Класс, содержащий конфигурацию для подключения к Redis
 *
 * @param redisProperties параметры подключения
 * */
@Configuration
class RedisConfig(val redisProperties: RedisProperties) {

    /**
     * Метод, создающий клиента Redis
     *
     * @return объект клиента Redis
     * */
    @Bean(destroyMethod = "shutdown")
    fun redisClient(): RedisClient {
        val URIBuilder = RedisURI.builder()
            .withHost(redisProperties.host)
            .withPort(redisProperties.port)
        if (redisProperties.password != null) {
            URIBuilder
                .withAuthentication(redisProperties.user, redisProperties.password)
        }
        return RedisClient.create(URIBuilder.build())
    }

    /**
     * Метод, открывающий соединение с Redis
     *
     * @return объект соединения с Redis
     * */
    @Bean(destroyMethod = "close")
    fun userStorageConnection(): StatefulRedisConnection<String, String> {
        return redisClient().connect()
    }

    /**
     * Lettuce использует один конект под капотом, поэтому если хочется делать транзакции,
     * то надо использовать отдельный коннект, чтобы в транзакцию не попали лишние команды
     * из других конектстов выполнения
     */
    @Bean(destroyMethod = "close")
    fun redisConnectionForTask(): StatefulRedisConnection<String, String> {
        return redisClient().connect()
    }

}