package ru.mirea.wordle.storage.redis

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.stereotype.Component
import ru.mirea.wordle.storage.WordRefresher
import ru.mirea.wordle.storage.redis.model.RedisTargetWordKey
import ru.mirea.wordle.storage.redis.model.RedisUserKey

@Component
class RedisWordRefresher(
    private val redisConnectionForTask: StatefulRedisConnection<String, String>,
) : WordRefresher {

    override fun updateWordAndRefreshUsersProgresses(chatId: String, newWord: String) {
        val commands = redisConnectionForTask.sync()
        commands.multi()
        commands.eval<Int>(
            DELETE_ALL_KEYS_BY_PREFIX_COMMAND,
            ScriptOutputType.INTEGER,
            arrayOf(),
            RedisUserKey.chatMembersWildcard(chatId)
        )
        commands.set(RedisTargetWordKey(chatId).toString(), newWord)
        commands.exec()
    }

    companion object {
        val DELETE_ALL_KEYS_BY_PREFIX_COMMAND = """
            local keys = redis.call("keys", ARGV[1]); 
            return #keys > 0 and redis.call("del", unpack(keys)) or 0
        """.trimIndent()
    }

    fun saveTargetWordFoChat(chatId: String, newWord: String) {
        val targetWordForChat = redisConnectionForTask.sync().get(RedisTargetWordKey(chatId).toString())
        if (targetWordForChat == null) {
            redisConnectionForTask.sync().set(RedisTargetWordKey(chatId).toString(), newWord)
        }
    }

}