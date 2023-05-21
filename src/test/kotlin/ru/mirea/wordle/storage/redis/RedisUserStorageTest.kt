package ru.mirea.wordle.storage.redis

import io.lettuce.core.api.StatefulRedisConnection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import ru.mirea.wordle.game.model.Color
import ru.mirea.wordle.game.model.GuessResult
import ru.mirea.wordle.game.model.Progress
import ru.mirea.wordle.game.model.User

@SpringBootTest
@ContextConfiguration(classes = [RedisUserStorageTest.Companion.TestConfig::class])
internal class RedisUserStorageTest {

    companion object {
        @Configuration
        @ComponentScan("ru.mirea.wordle.storage.redis")
        @ComponentScan("ru.mirea.wordle.config.redis")
        @ConfigurationPropertiesScan(value = ["ru.mirea.wordle.config"])
        class TestConfig {}
    }

    @Autowired
    lateinit var redisUserStorage: RedisUserStorage

    @Autowired
    lateinit var userStorageConnection: StatefulRedisConnection<String, String>

    init {
        val redis = GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379)
        redis.start()
        System.setProperty("redis.host", redis.getHost())
        System.setProperty("redis.port", redis.getMappedPort(6379).toString())
    }

    @AfterEach
    fun clear() {
        userStorageConnection.sync().flushall()
    }

    @Test
    fun putAndGet() {
        val user = User(
            chatId = "1",
            userId = "1",
            progress = Progress(
                tries = listOf(GuessResult(mutableListOf(GuessResult.Letter('a', Color.GREY)))),
                won = false
            ),
            score = 0
        )
        redisUserStorage.updateUser(user)
        val userFromRedis = redisUserStorage.getUser(user.chatId, user.userId)
        Assertions.assertEquals(user, userFromRedis)
    }

}