package ru.mirea.wordle.storage.redis

import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.stereotype.Repository
import ru.mirea.wordle.game.model.User
import ru.mirea.wordle.storage.TargetWordStorage
import ru.mirea.wordle.storage.redis.exception.WordNotFoundException
import ru.mirea.wordle.storage.redis.model.RedisTargetWordKey

@Repository
class RedisTargetWordStorage(
    private val userStorageConnection: StatefulRedisConnection<String, String>
) : TargetWordStorage {

    override fun getTargetWordForUser(user: User): String {
        return userStorageConnection.sync().get(
            RedisTargetWordKey(user.chatId).toString()
        ) ?: throw WordNotFoundException(user.chatId)
    }
}