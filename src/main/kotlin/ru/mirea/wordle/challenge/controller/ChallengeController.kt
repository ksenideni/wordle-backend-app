package ru.mirea.wordle.challenge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.challenge.service.StudentAttemptService
import ru.mirea.wordle.challenge.service.ChallengeService
import java.time.LocalDate

data class CreateClassChallengeRequest(
    val date: String? = null,
    val dictionary_id: Int? = null,
    val word: String? = null
)

data class CreateStudentChallengeRequest(
    val date: String? = null,
    val dictionary_id: Int,
    val word: String? = null
)

data class CreateIndividualChallengesRequest(
    val date: String? = null,
    val dictionary_id: Int
)

data class ChallengeResponse(
    val id: Int,
    val date: String,
    val word: String,
    val dictionary_id: Int,
    val class_id: Int? = null,
    val user_id: Int? = null,
    val status: String,
    val created_at: String? = null
)

data class TodayChallengeResponse(
    val id: Int,
    val date: String,
    val word: String,
    val dictionary_id: Int,
    val status: String,
    val attempts: List<AttemptResponse>,
    val remaining_attempts: Int,
    val is_completed: Boolean
)

data class AttemptResponse(
    val id: Int,
    val attempt_number: Int,
    val guessed_word: String,
    val result: Any,
    val points: Int,
    val completed_at: String? = null
)

data class ChallengeHistoryItem(
    val id: Int,
    val date: String,
    val word: String,
    val status: String,
    val attempts_count: Int,
    val points: Int,
    val completed_at: String? = null
)

data class ChallengeHistoryResponse(
    val challenges: List<ChallengeHistoryItem>
)

data class IndividualChallengeItem(
    val user_id: Int,
    val word: String,
    val challenge_id: Int
)

data class IndividualChallengesResponse(
    val created_count: Int,
    val challenges: List<IndividualChallengeItem>
)

@RestController
@RequestMapping("/challenges")
class ChallengeController(
    private val challengeService: ChallengeService,
    private val attemptService: StudentAttemptService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/today")
    @PreAuthorize("hasRole('STUDENT')")
    fun getTodayChallenge(authentication: Authentication): ResponseEntity<TodayChallengeResponse> {
        val userId = challengeService.getUserIdFromAuthentication(authentication)
        val challenge = challengeService.getTodayChallenge(userId)
        val attempts = attemptService.getAttemptsByChallenge(challenge.id!!, userId)

        val attemptsResponse = attempts.map { attempt ->
            val result = objectMapper.readValue(attempt.result, Map::class.java)
            AttemptResponse(
                id = attempt.id!!,
                attempt_number = attempt.attemptNumber,
                guessed_word = attempt.guessedWord,
                result = result,
                points = attempt.points,
                completed_at = attempt.completedAt?.toString()
            )
        }

        val isCompleted = challenge.status == "completed" || challenge.status == "expired"
        val wordToShow = if (isCompleted) challenge.word else "*****"

        return ResponseEntity.ok(
            TodayChallengeResponse(
                id = challenge.id,
                date = challenge.date.toString(),
                word = wordToShow,
                dictionary_id = challenge.dictionaryId,
                status = challenge.status,
                attempts = attemptsResponse,
                remaining_attempts = 6 - attempts.size,
                is_completed = isCompleted
            )
        )
    }

    @PostMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun createClassChallenge(
        @PathVariable classId: Int,
        @RequestBody request: CreateClassChallengeRequest,
        authentication: Authentication
    ): ResponseEntity<ChallengeResponse> {
        val teacherId = challengeService.getTeacherIdFromAuthentication(authentication)
        val date = request.date?.let { LocalDate.parse(it) }
        val challenge = challengeService.createClassChallenge(
            classId = classId,
            date = date,
            dictionaryId = request.dictionary_id,
            word = request.word,
            teacherId = teacherId
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ChallengeResponse(
                id = challenge.id!!,
                date = challenge.date.toString(),
                word = challenge.word,
                dictionary_id = challenge.dictionaryId,
                class_id = challenge.classId,
                user_id = challenge.userId,
                status = challenge.status,
                created_at = challenge.createdAt?.toString()
            )
        )
    }

    @PostMapping("/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun createStudentChallenge(
        @PathVariable studentId: Int,
        @RequestBody request: CreateStudentChallengeRequest,
        authentication: Authentication
    ): ResponseEntity<ChallengeResponse> {
        val teacherId = challengeService.getTeacherIdFromAuthentication(authentication)
        val date = request.date?.let { LocalDate.parse(it) }
        val challenge = challengeService.createStudentChallenge(
            studentId = studentId,
            date = date,
            dictionaryId = request.dictionary_id,
            word = request.word,
            teacherId = teacherId
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ChallengeResponse(
                id = challenge.id!!,
                date = challenge.date.toString(),
                word = challenge.word,
                dictionary_id = challenge.dictionaryId,
                class_id = challenge.classId,
                user_id = challenge.userId,
                status = challenge.status,
                created_at = challenge.createdAt?.toString()
            )
        )
    }

    @PostMapping("/class/{classId}/individual")
    @PreAuthorize("hasRole('TEACHER')")
    fun createIndividualChallenges(
        @PathVariable classId: Int,
        @RequestBody request: CreateIndividualChallengesRequest,
        authentication: Authentication
    ): ResponseEntity<IndividualChallengesResponse> {
        val teacherId = challengeService.getTeacherIdFromAuthentication(authentication)
        val date = request.date?.let { LocalDate.parse(it) }
        val challenges = challengeService.createIndividualChallengesForClass(
            classId = classId,
            date = date,
            dictionaryId = request.dictionary_id,
            teacherId = teacherId
        )

        val challengesResponse = challenges.map { challenge ->
            IndividualChallengeItem(
                user_id = challenge.userId!!,
                word = challenge.word,
                challenge_id = challenge.id!!
            )
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
            IndividualChallengesResponse(
                created_count = challenges.size,
                challenges = challengesResponse
            )
        )
    }

    @GetMapping("/history")
    fun getChallengeHistory(
        @RequestParam(required = false) start_date: String?,
        @RequestParam(required = false) end_date: String?,
        @RequestParam(required = false, defaultValue = "30") limit: Int,
        authentication: Authentication
    ): ResponseEntity<ChallengeHistoryResponse> {
        val userId = challengeService.getUserIdFromAuthentication(authentication)
        val startDate = start_date?.let { LocalDate.parse(it) }
        val endDate = end_date?.let { LocalDate.parse(it) }
        val challenges = challengeService.getChallengeHistory(userId, startDate, endDate, limit)

        val challengesResponse = challenges.map { challenge ->
            val attempts = attemptService.getAttemptsByChallenge(challenge.id!!, userId)
            val totalPoints = attempts.sumOf { it.points }
            val completedAt = attempts.lastOrNull()?.completedAt

            ChallengeHistoryItem(
                id = challenge.id,
                date = challenge.date.toString(),
                word = challenge.word,
                status = challenge.status,
                attempts_count = attempts.size,
                points = totalPoints,
                completed_at = completedAt?.toString()
            )
        }

        return ResponseEntity.ok(ChallengeHistoryResponse(challenges = challengesResponse))
    }
}

