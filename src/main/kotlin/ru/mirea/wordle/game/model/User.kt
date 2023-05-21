package ru.mirea.wordle.game.model

/**
 * Класс, хранящий информацию о пользователе
 *
 * */
data class User(
    val chatId: String,
    val userId: String,
    var progress: Progress,
    val score: Int
)