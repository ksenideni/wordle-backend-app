package ru.mirea.wordle.challenge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.challenge.service.StudentAttemptService

data class MakeAttemptRequest(
    val guessed_word: String,
    val challenge_id: Int? = null // опционально, если не указан - используется вызов на сегодня
)

data class MakeAttemptResponse(
    val id: Int,
    val challenge_id: Int,
    val attempt_number: Int,
    val guessed_word: String,
    val result: Any,
    val points: Int,
    val remaining_attempts: Int,
    val completed_at: String? = null
)

data class AttemptListItem(
    val id: Int,
    val attempt_number: Int,
    val guessed_word: String,
    val result: Any,
    val points: Int,
    val completed_at: String? = null
)

data class AttemptsListResponse(
    val attempts: List<AttemptListItem>,
    val total_attempts: Int,
    val remaining_attempts: Int
)

@RestController
@RequestMapping("/attempts")
class StudentAttemptController(
    private val attemptService: StudentAttemptService,
    private val objectMapper: ObjectMapper
) {

    @PostMapping
    fun makeAttempt(
        @RequestBody request: MakeAttemptRequest,
        authentication: Authentication
    ): ResponseEntity<MakeAttemptResponse> {
        val userId = attemptService.getUserIdFromAuthentication(authentication)
        val attempt = attemptService.makeAttempt(request.guessed_word, userId, request.challenge_id)

        val result = objectMapper.readValue(attempt.result, Map::class.java)
        val remainingAttempts = 6 - attempt.attemptNumber

        return ResponseEntity.ok(
            MakeAttemptResponse(
                id = attempt.id!!,
                challenge_id = attempt.challengeId,
                attempt_number = attempt.attemptNumber,
                guessed_word = attempt.guessedWord,
                result = result,
                points = attempt.points,
                remaining_attempts = remainingAttempts,
                completed_at = attempt.completedAt?.toString()
            )
        )
    }

    @GetMapping("/challenge/{challengeId}")
    fun getAttemptsByChallenge(
        @PathVariable challengeId: Int,
        authentication: Authentication
    ): ResponseEntity<AttemptsListResponse> {
        val userId = attemptService.getUserIdFromAuthentication(authentication)
        val attempts = attemptService.getAttemptsByChallenge(challengeId, userId)

        val attemptsResponse = attempts.map { attempt ->
            val result = objectMapper.readValue(attempt.result, Map::class.java)
            AttemptListItem(
                id = attempt.id!!,
                attempt_number = attempt.attemptNumber,
                guessed_word = attempt.guessedWord,
                result = result,
                points = attempt.points,
                completed_at = attempt.completedAt?.toString()
            )
        }

        return ResponseEntity.ok(
            AttemptsListResponse(
                attempts = attemptsResponse,
                total_attempts = attempts.size,
                remaining_attempts = 6 - attempts.size
            )
        )
    }
}

