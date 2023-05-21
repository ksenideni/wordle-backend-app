package ru.mirea.wordle.storage.redis.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.stereotype.Component
import ru.mirea.wordle.game.model.User

/**
 * Класс, предназначенный для преобразования объекто пользователя в JSON, и наоборот.
 *
 * */
@Component
class UserMapper {

    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    /**
     * Метод, преобразующий строку в формате JSON в объект пользователя
     *
     * @param json Строка в формате JSON, хранящая информацию о пользователе
     * @return Объект, хранящий информацию о пользователе
     * */
    fun mapToModel(json: String): User {
        return objectMapper.readValue(json, User::class.java)
    }

    /**
     * Метод, преобразующий Объект, хранящий информацию о пользователе в строку в формате JSON
     *
     * @param user Объект, хранящий информацию о пользователе
     * @return Строка в формате JSON, хранящая информацию о пользователе
     * */
    fun mapToJson(user: User): String {
        return objectMapper.writeValueAsString(user)
    }

}