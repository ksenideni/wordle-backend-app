package ru.mirea.wordle.storage.redis

import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.stereotype.Repository
import ru.mirea.wordle.game.model.Progress
import ru.mirea.wordle.game.model.User
import ru.mirea.wordle.storage.UserStorage
import ru.mirea.wordle.storage.redis.exception.UserNotFoundException
import ru.mirea.wordle.storage.redis.model.RedisTargetWordKey
import ru.mirea.wordle.storage.redis.model.RedisUserKey
import ru.mirea.wordle.storage.redis.model.UserMapper
import ru.mirea.wordle.task.WordCreateStrategy

/**
 * Класс, необходимый для взаимодейтвия с Redis и работой с информацией о пользователе
 *
 * @param userMapper объект класса для преобразования объекта пользователя в JSON, и обратно
 * @param statefulRedisConnection объект соединения с Redis
 *
 * */
@Repository
class RedisUserStorage(
    val userMapper: UserMapper,
    val userStorageConnection: StatefulRedisConnection<String, String>,
    val redisWordRefresher: RedisWordRefresher,
    val wordCreateStrategy: WordCreateStrategy,
) : UserStorage {

    /**
     * Метод, предназначенный для поиска пользователя в хранилище.
     *
     * @param chatId индентификатор чата, в котором находится пользователь
     * @param userId индентификатор пользователя
     *
     * @return объект, хранящий информацию о пользователе
     * @throws UserNotFoundException
     * */
    override fun getUser(chatId: String, userId: String): User {
        var result = userStorageConnection.sync().get(RedisUserKey(chatId, userId).toString())
        if (result == null) {
            createUser(chatId, userId)
            result = userStorageConnection.sync().get(RedisUserKey(chatId, userId).toString())
        }
        createTargetWordForChatIfDoesntExist(chatId);
        var model = userMapper.mapToModel(result)
        return model
    }

    /**
     * Метод, предназначенный для обновления информации о пользователе в хранилище
     *
     * @param user объект, хранящий обновленную информацию о пользователе и его прогрессе
     * */
    override fun updateUser(user: User) {
        userStorageConnection.sync().set(
            RedisUserKey(user.chatId, user.userId).toString(),
            userMapper.mapToJson(user)
        )
    }

    fun createUser(chatId: String, userId: String): String? {
        val user = User(chatId, userId, Progress(false, listOf()), 0);
        return userStorageConnection.sync().set(
            RedisUserKey(user.chatId, user.userId).toString(),
            userMapper.mapToJson(user)
        )
    }

    fun createTargetWordForChatIfDoesntExist(chatId: String) {
        redisWordRefresher.saveTargetWordFoChat(chatId, wordCreateStrategy.newWord())
    }
}