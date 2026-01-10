package ru.mirea.wordle.ranking.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.ranking.service.RankingService
import ru.mirea.wordle.user.repository.UserRepository
import ru.mirea.wordle.`class`.repository.ClassRepository

data class ClassRankingItem(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val totalPoints: Int,
    val dailyPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val classRank: Int?,
    val lastActivity: String? = null
)

data class ClassRankingResponse(
    val classId: Int,
    val period: String,
    val rankings: List<ClassRankingItem>
)

data class GlobalRankingItem(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val totalPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val globalRank: Int,
    val className: String? = null
)

data class GlobalRankingResponse(
    val rankings: List<GlobalRankingItem>
)

data class UserStatisticsResponse(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val login: String?,
    val totalPoints: Int,
    val dailyPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val globalRank: Int,
    val classRank: Int?,
    val totalChallenges: Int,
    val completedChallenges: Int,
    val successRate: Double,
    val averageAttempts: Double,
    val lastActivity: String? = null
)

data class StreakHistoryItem(
    val date: String,
    val completed: Boolean
)

data class StreakHistoryResponse(
    val currentStreak: Int,
    val longestStreak: Int,
    val streakHistory: List<StreakHistoryItem>
)

@RestController
@RequestMapping("/rankings")
class RankingController(
    private val rankingService: RankingService,
    private val userRepository: UserRepository,
    private val classRepository: ClassRepository
) {

    @GetMapping("/class/{classId}")
    fun getClassRanking(
        @PathVariable classId: Int,
        @RequestParam(required = false, defaultValue = "all") period: String,
        authentication: Authentication
    ): ResponseEntity<ClassRankingResponse> {
        val rankings = rankingService.getClassRanking(classId, period)
        
        val rankingsResponse = rankings.map { ranking ->
            val user = userRepository.findById(ranking.userId).orElse(null)
            ClassRankingItem(
                userId = ranking.userId,
                firstName = user?.firstName ?: "",
                lastName = user?.lastName ?: "",
                totalPoints = ranking.totalPoints,
                dailyPoints = ranking.dailyPoints,
                currentStreak = ranking.currentStreak,
                longestStreak = ranking.longestStreak,
                classRank = ranking.classRank,
                lastActivity = ranking.updatedAt?.toString()
            )
        }

        return ResponseEntity.ok(
            ClassRankingResponse(
                classId = classId,
                period = period,
                rankings = rankingsResponse
            )
        )
    }

    @GetMapping("/global")
    fun getGlobalRanking(
        @RequestParam(required = false, defaultValue = "100") limit: Int
    ): ResponseEntity<GlobalRankingResponse> {
        val rankings = rankingService.getGlobalRanking(limit)
        
        val rankingsResponse = rankings.map { ranking ->
            val user = userRepository.findById(ranking.userId).orElse(null)
            val className = ranking.classId?.let { classId ->
                classRepository.findById(classId).orElse(null)?.name
            }
            
            GlobalRankingItem(
                userId = ranking.userId,
                firstName = user?.firstName ?: "",
                lastName = user?.lastName ?: "",
                totalPoints = ranking.totalPoints,
                currentStreak = ranking.currentStreak,
                longestStreak = ranking.longestStreak,
                globalRank = ranking.globalRank ?: 0,
                className = className
            )
        }

        return ResponseEntity.ok(GlobalRankingResponse(rankings = rankingsResponse))
    }

    @GetMapping("/user/{userId}")
    fun getUserStatistics(
        @PathVariable userId: Int,
        authentication: Authentication
    ): ResponseEntity<UserStatisticsResponse> {
        val statistics = rankingService.getUserStatistics(userId)
        
        return ResponseEntity.ok(
            UserStatisticsResponse(
                userId = statistics.userId,
                firstName = statistics.firstName,
                lastName = statistics.lastName,
                login = statistics.login,
                totalPoints = statistics.totalPoints,
                dailyPoints = statistics.dailyPoints,
                currentStreak = statistics.currentStreak,
                longestStreak = statistics.longestStreak,
                globalRank = statistics.globalRank,
                classRank = statistics.classRank,
                totalChallenges = statistics.totalChallenges,
                completedChallenges = statistics.completedChallenges,
                successRate = statistics.successRate,
                averageAttempts = statistics.averageAttempts,
                lastActivity = statistics.lastActivity?.toString()
            )
        )
    }

    @GetMapping("/me")
    fun getMyStatistics(
        authentication: Authentication
    ): ResponseEntity<UserStatisticsResponse> {
        val userId = rankingService.getUserIdFromAuthentication(authentication)
        val statistics = rankingService.getUserStatistics(userId)
        
        return ResponseEntity.ok(
            UserStatisticsResponse(
                userId = statistics.userId,
                firstName = statistics.firstName,
                lastName = statistics.lastName,
                login = statistics.login,
                totalPoints = statistics.totalPoints,
                dailyPoints = statistics.dailyPoints,
                currentStreak = statistics.currentStreak,
                longestStreak = statistics.longestStreak,
                globalRank = statistics.globalRank,
                classRank = statistics.classRank,
                totalChallenges = statistics.totalChallenges,
                completedChallenges = statistics.completedChallenges,
                successRate = statistics.successRate,
                averageAttempts = statistics.averageAttempts,
                lastActivity = statistics.lastActivity?.toString()
            )
        )
    }

    @GetMapping("/me/streaks")
    fun getStreakHistory(
        @RequestParam(required = false, defaultValue = "30") days: Int,
        authentication: Authentication
    ): ResponseEntity<StreakHistoryResponse> {
        val userId = rankingService.getUserIdFromAuthentication(authentication)
        val streakHistory = rankingService.getStreakHistory(userId, days)
        return ResponseEntity.ok(streakHistory)
    }
}

