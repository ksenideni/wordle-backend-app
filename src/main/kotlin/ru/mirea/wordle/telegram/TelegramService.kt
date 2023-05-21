package ru.mirea.wordle.telegram

/**
 * Интерфейс для взаимодействия с Telegram API.
 *
 * */
interface TelegramService {
    /**
     * Метод, для увеличения количества очков пользователя.
     *
     * @param userId идентификатор пользователя
     * @param chatId идентификатор чата
     * @param delta число, на которое будет увеличено количество очков пользователя в чате
     * */
    fun incrementScore(userId: String, chatId: String, messageId: String, delta: Int)
}