package ru.mirea.wordle.storage

import ru.mirea.wordle.game.model.User

/**
 * Интерфейс, предназначенный для работы с информацией о пользователях в хранилище
 *
 * */
interface UserStorage {
    /**
     * Метод, предназначенный для поиска пользователя в хранилище.
     *
     * @param chatId индентификатор чата, в котором находится пользователь
     * @param userId индентификатор пользователя
     *
     * @return объект, хранящий информацию о пользователе
     * @throws UserNotFoundException
     * */
    fun getUser(chatId: String, userId: String): User

    /**
     * Метод, предназначенный для обновления информации о пользователе в хранилище
     *
     * @param user объект, хранящий обновленную информацию о пользователе и его прогрессе
     * */
    fun updateUser(user: User)
}