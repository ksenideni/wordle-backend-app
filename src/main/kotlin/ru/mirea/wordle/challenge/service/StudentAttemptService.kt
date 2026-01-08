package ru.mirea.wordle.challenge.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.mirea.wordle.challenge.model.Attempt
import ru.mirea.wordle.challenge.repository.StudentAttemptRepository
import ru.mirea.wordle.challenge.repository.DailyChallengeRepository
import ru.mirea.wordle.challenge.service.ChallengeService
import ru.mirea.wordle.dictionary.repository.DictionaryWordRepository
import ru.mirea.wordle.user.repository.UserRepository
import ru.mirea.wordle.user.service.UserService
import java.time.LocalDateTime

data class LetterPosition(
    val letter: String,
    val color: String,
    val position: Int
)

data class AttemptResult(
    val positions: List<LetterPosition>,
    val is_correct: Boolean
)

@Service
class StudentAttemptService(
    private val attemptRepository: StudentAttemptRepository,
    private val challengeRepository: DailyChallengeRepository,
    private val challengeService: ChallengeService,
    private val dictionaryWordRepository: DictionaryWordRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {

    @Transactional
    fun makeAttempt(guessedWord: String, userId: Int, challengeId: Int? = null): Attempt {
        // Преобразуем слово в верхний регистр сразу
        val guessedWordUpper = guessedWord.uppercase()
        
        // Если challenge_id не указан, находим вызов на сегодня
        val challenge = if (challengeId != null) {
            challengeRepository.findById(challengeId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found") }
        } else {
            challengeService.getTodayChallenge(userId)
        }
        
        val finalChallengeId = challenge.id!!

        // Проверяем права доступа: либо это индивидуальный вызов для этого пользователя, либо классовый вызов
        if (challenge.userId != null && challenge.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to make attempts for this challenge")
        }
        
        // Для классовых вызовов проверяем, что пользователь в этом классе
        if (challenge.userId == null && challenge.classId != null) {
            val user = userRepository.findById(userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
            if (user.classId != challenge.classId) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to make attempts for this challenge")
            }
        }

        if (challenge.status != "active") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge is not active")
        }

        // Проверяем длину слова
        if (guessedWordUpper.length != 5) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Word must be 5 letters long")
        }

        // Получаем текущее количество попыток
        val currentAttemptCount = attemptRepository.countByChallengeIdAndUserId(finalChallengeId, userId)
        if (currentAttemptCount >= 6) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum attempts reached")
        }

        val attemptNumber = currentAttemptCount + 1
        val targetWord = challenge.word.uppercase()

        // Вычисляем результат
        val result = calculateResult(guessedWordUpper, targetWord)
        val isCorrect = guessedWordUpper == targetWord

        // Вычисляем очки
        val points = calculatePoints(attemptNumber, isCorrect)

        // Обновляем статус вызова, если угадали или закончились попытки
        val isCompleted = isCorrect || attemptNumber >= 6
        if (isCompleted) {
            challenge.status = if (isCorrect) "completed" else "expired"
            challenge.updatedAt = LocalDateTime.now()
            challengeRepository.save(challenge)
        }

        val now = LocalDateTime.now()
        val attempt = Attempt(
            userId = userId,
            challengeId = finalChallengeId,
            attemptNumber = attemptNumber,
            guessedWord = guessedWordUpper,
            result = objectMapper.writeValueAsString(result),
            points = points,
            completedAt = now,
            createdAt = now
        )

        return attemptRepository.save(attempt)
    }

    fun getAttemptsByChallenge(challengeId: Int, userId: Int): List<Attempt> {
        val challenge = challengeRepository.findById(challengeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found") }

        // Проверяем права доступа: либо это индивидуальный вызов для этого пользователя, либо классовый вызов
        if (challenge.userId != null && challenge.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view attempts for this challenge")
        }

        return attemptRepository.findByChallengeIdAndUserId(challengeId, userId)
            .sortedBy { it.attemptNumber }
    }

    private fun calculateResult(guessedWord: String, targetWord: String): AttemptResult {
        val positions = mutableListOf<LetterPosition>()
        val targetChars = targetWord.toCharArray()
        val guessedChars = guessedWord.toCharArray()
        val used = BooleanArray(5)

        // Сначала отмечаем зеленые (правильные позиции)
        for (i in targetChars.indices) {
            if (guessedChars[i] == targetChars[i]) {
                positions.add(LetterPosition(guessedChars[i].toString(), "green", i))
                used[i] = true
            } else {
                positions.add(LetterPosition(guessedChars[i].toString(), "", i))
            }
        }

        // Затем отмечаем желтые (буква есть, но не на правильной позиции)
        for (i in guessedChars.indices) {
            if (positions[i].color == "green") continue

            for (j in targetChars.indices) {
                if (!used[j] && guessedChars[i] == targetChars[j]) {
                    positions[i] = LetterPosition(guessedChars[i].toString(), "yellow", i)
                    used[j] = true
                    break
                }
            }
        }

        // Остальные - серые
        for (i in positions.indices) {
            if (positions[i].color.isEmpty()) {
                positions[i] = LetterPosition(positions[i].letter, "gray", i)
            }
        }

        return AttemptResult(positions, guessedWord == targetWord)
    }

    private fun calculatePoints(attemptNumber: Int, isCorrect: Boolean): Int {
        if (!isCorrect) return 0

        return when (attemptNumber) {
            1 -> 100
            2 -> 80
            3 -> 60
            4 -> 40
            5 -> 20
            6 -> 10
            else -> 0
        }
    }

    fun getUserIdFromAuthentication(authentication: Authentication): Int {
        val username = authentication.name
        val user = userService.findByEmail(username) ?: userService.findByLogin(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        return user.id ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User ID is null")
    }
}

