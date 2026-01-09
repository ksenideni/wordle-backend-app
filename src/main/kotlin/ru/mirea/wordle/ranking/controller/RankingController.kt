package ru.mirea.wordle.ranking.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.ranking.service.RankingService
import ru.mirea.wordle.user.repository.UserRepository
import ru.mirea.wordle.`class`.repository.ClassRepository

data class ClassRankingItem(
    val user_id: Int,
    val first_name: String,
    val last_name: String,
    val total_points: Int,
    val daily_points: Int,
    val current_streak: Int,
    val longest_streak: Int,
    val class_rank: Int?,
    val last_activity: String? = null
)

data class ClassRankingResponse(
    val class_id: Int,
    val period: String,
    val rankings: List<ClassRankingItem>
)

data class GlobalRankingItem(
    val user_id: Int,
    val first_name: String,
    val last_name: String,
    val total_points: Int,
    val current_streak: Int,
    val longest_streak: Int,
    val global_rank: Int,
    val class_name: String? = null
)

data class GlobalRankingResponse(
    val rankings: List<GlobalRankingItem>
)

data class UserStatisticsResponse(
    val user_id: Int,
    val first_name: String,
    val last_name: String,
    val login: String?,
    val total_points: Int,
    val daily_points: Int,
    val current_streak: Int,
    val longest_streak: Int,
    val global_rank: Int,
    val class_rank: Int?,
    val total_challenges: Int,
    val completed_challenges: Int,
    val success_rate: Double,
    val average_attempts: Double,
    val last_activity: String? = null
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
                user_id = ranking.userId,
                first_name = user?.firstName ?: "",
                last_name = user?.lastName ?: "",
                total_points = ranking.totalPoints,
                daily_points = ranking.dailyPoints,
                current_streak = ranking.currentStreak,
                longest_streak = ranking.longestStreak,
                class_rank = ranking.classRank,
                last_activity = ranking.updatedAt?.toString()
            )
        }

        return ResponseEntity.ok(
            ClassRankingResponse(
                class_id = classId,
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
                user_id = ranking.userId,
                first_name = user?.firstName ?: "",
                last_name = user?.lastName ?: "",
                total_points = ranking.totalPoints,
                current_streak = ranking.currentStreak,
                longest_streak = ranking.longestStreak,
                global_rank = ranking.globalRank ?: 0,
                class_name = className
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
                user_id = statistics.userId,
                first_name = statistics.firstName,
                last_name = statistics.lastName,
                login = statistics.login,
                total_points = statistics.totalPoints,
                daily_points = statistics.dailyPoints,
                current_streak = statistics.currentStreak,
                longest_streak = statistics.longestStreak,
                global_rank = statistics.globalRank,
                class_rank = statistics.classRank,
                total_challenges = statistics.totalChallenges,
                completed_challenges = statistics.completedChallenges,
                success_rate = statistics.successRate,
                average_attempts = statistics.averageAttempts,
                last_activity = statistics.lastActivity?.toString()
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
                user_id = statistics.userId,
                first_name = statistics.firstName,
                last_name = statistics.lastName,
                login = statistics.login,
                total_points = statistics.totalPoints,
                daily_points = statistics.dailyPoints,
                current_streak = statistics.currentStreak,
                longest_streak = statistics.longestStreak,
                global_rank = statistics.globalRank,
                class_rank = statistics.classRank,
                total_challenges = statistics.totalChallenges,
                completed_challenges = statistics.completedChallenges,
                success_rate = statistics.successRate,
                average_attempts = statistics.averageAttempts,
                last_activity = statistics.lastActivity?.toString()
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

