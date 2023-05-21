package ru.mirea.wordle.game.service

import org.springframework.stereotype.Service
import ru.mirea.wordle.game.model.Progress
import ru.mirea.wordle.storage.redis.RedisTargetWordStorage
import ru.mirea.wordle.storage.redis.RedisUserStorage
import ru.mirea.wordle.telegram.TelegramService

@Service
class AttemptService(
    val wordleService: WordleService,
    val userStorage: RedisUserStorage,
    val wordStorage: RedisTargetWordStorage,
    val telegramService: TelegramService
) {

    fun getAttempts(chatId: String, userId: String): Progress {
        val user = userStorage.getUser(chatId, userId)
        return user.progress

    }

    fun postAttempt(chatId: String, userId: String, messageId: String, currentWord: String): Progress {
        val user = userStorage.getUser(chatId, userId)
        val targetWord = wordStorage.getTargetWordForUser(user)
        val progress = wordleService.makeTry(user.progress, currentWord, targetWord)
        if (progress.won) {
            var score = 6 - progress.tries.size
            telegramService.incrementScore(userId, chatId, messageId, score)
        }
        user.progress = progress
        userStorage.updateUser(user)
        return progress
    }
}