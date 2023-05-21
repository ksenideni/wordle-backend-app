package ru.mirea.wordle.game.service

import ru.mirea.wordle.game.model.Progress

/**
 * Сервис, содержащий логику совершения попытки пользователем в игре Wordle.
 *
 * Данный интерфейс содержит логику, предназначенную для совершения попытки угадать слово пользователем.
 *
 * */
interface WordleService {
    /**
     * Метод для совершения попытки пользователя угадать слово.
     * @param user объект пользователя игры, пытающегося угадать слово
     * @param currentWord предположение пользователя о слове, загаданном игрой
     * @param targetWord слово, которое пользователь пытается угадать
     *
     * @return прогресс пользователя после совершения попытки
     * @throws AlreadyWonException
     * */
    fun makeTry(progress: Progress, currentWord: String, targetWord: String): Progress
}