package ru.mirea.wordle.game.model

/**
 * Класс, представляющий текущий прогресс пользователя по угадыванию слова.
 *
 * */
data class Progress(
    val won: Boolean,
    val tries: List<GuessResult>
)
