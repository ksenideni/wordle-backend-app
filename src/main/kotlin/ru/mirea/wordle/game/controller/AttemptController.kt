package ru.mirea.wordle.game.controller

import Wrapper
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.game.model.Progress
import ru.mirea.wordle.game.service.AttemptService

@RestController
@RequestMapping("/wordle/attempts")
class AttemptController(
    val attemptService: AttemptService
) {

    @GetMapping
    fun getAttempts(
        @RequestParam chatId: String,
        @RequestParam userId: String
    ): Progress {
        return attemptService.getAttempts(chatId, userId)
    }

    @PostMapping
    fun postAttempt(
        @RequestParam chatId: String,
        @RequestParam userId: String,
        @RequestParam messageId: String,
        @RequestBody wrapper: Wrapper
    ): Progress {
        return attemptService.postAttempt(chatId, userId, messageId, wrapper.currentWord)
    }
}
