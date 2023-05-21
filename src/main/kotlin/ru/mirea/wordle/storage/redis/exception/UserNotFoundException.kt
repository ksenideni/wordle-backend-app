package ru.mirea.wordle.storage.redis.exception

/**
 * Исключение, которое выбрасывается, если пользователя не удалось найти в хранилище
 *
 * */
class UserNotFoundException(chatId: String, userId: String): RuntimeException("User with id $userId now found in $chatId") {
}