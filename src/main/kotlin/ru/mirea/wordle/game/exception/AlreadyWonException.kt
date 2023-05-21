package ru.mirea.wordle.game.exception

/**
 * Исключение, которое выбрасывается, если пользователь повторно пытается угадать слово после завершения игры.
 *
 * */
class AlreadyWonException: RuntimeException("User already won") {
}