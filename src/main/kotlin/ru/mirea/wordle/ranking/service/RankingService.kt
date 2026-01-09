package ru.mirea.wordle.ranking.service

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.mirea.wordle.challenge.repository.DailyChallengeRepository
import ru.mirea.wordle.challenge.repository.StudentAttemptRepository
import ru.mirea.wordle.`class`.repository.ClassRepository
import ru.mirea.wordle.ranking.controller.StreakHistoryItem
import ru.mirea.wordle.ranking.controller.StreakHistoryResponse
import ru.mirea.wordle.ranking.model.Ranking
import ru.mirea.wordle.ranking.repository.RankingRepository
import ru.mirea.wordle.user.repository.UserRepository
import ru.mirea.wordle.user.service.UserService
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class RankingService(
    private val rankingRepository: RankingRepository,
    private val challengeRepository: DailyChallengeRepository,
    private val attemptRepository: StudentAttemptRepository,
    private val userRepository: UserRepository,
    private val classRepository: ClassRepository,
    private val userService: UserService
) {

    @Transactional
    fun updateRankingAfterAttempt(userId: Int, challengeId: Int, points: Int, isCompleted: Boolean) {
        if (!isCompleted || points == 0) return // Обновляем только при успешном завершении

        val challenge = challengeRepository.findById(challengeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        // Для классовых вызовов проверяем, что пользователь в классе
        if (challenge.userId == null && challenge.classId != null) {
            if (user.classId != challenge.classId) {
                return // Пользователь не в этом классе, не обновляем рейтинг
            }
        }

        val challengeDate = challenge.date // Используем дату вызова
        val now = LocalDateTime.now()

        // Получаем или создаем рейтинг на дату вызова
        var ranking = rankingRepository.findByUserIdAndDate(userId, challengeDate)

        if (ranking == null) {
            // Получаем последний рейтинг для вычисления стрика
            val lastRanking = rankingRepository.findByUserIdOrderByDateDesc(userId).firstOrNull()
            val previousDate = lastRanking?.date
            val previousDay = challengeDate.minusDays(1)

            val currentStreak = if (previousDate == previousDay && lastRanking != null && lastRanking.dailyPoints > 0) {
                lastRanking.currentStreak + 1
            } else {
                1
            }

            val longestStreak = lastRanking?.longestStreak ?: 0
            val newLongestStreak = if (currentStreak > longestStreak) currentStreak else longestStreak

            ranking = Ranking(
                userId = userId,
                classId = user.classId,
                date = challengeDate,
                dailyPoints = points,
                totalPoints = (lastRanking?.totalPoints ?: 0) + points,
                currentStreak = currentStreak,
                longestStreak = newLongestStreak,
                createdAt = now,
                updatedAt = now
            )
        } else {
            // Обновляем существующий рейтинг
            ranking.dailyPoints += points
            ranking.totalPoints += points
            ranking.updatedAt = now
        }

        ranking = rankingRepository.save(ranking)

        // Обновляем ранги на дату вызова
        updateRanks(challengeDate, user.classId)
    }

    @Transactional
    private fun updateRanks(date: LocalDate, classId: Int?) {
        // Обновляем глобальные ранги
        val globalRankings = rankingRepository.findGlobalRankingsByDate(date)
        globalRankings.forEachIndexed { index, ranking ->
            ranking.globalRank = index + 1
            rankingRepository.save(ranking)
        }

        // Обновляем ранги в классе
        if (classId != null) {
            val classRankings = rankingRepository.findClassRankingsByDate(classId, date)
            classRankings.forEachIndexed { index, ranking ->
                ranking.classRank = index + 1
                rankingRepository.save(ranking)
            }
        }
    }

    fun getClassRanking(classId: Int, period: String): List<Ranking> {
        val today = LocalDate.now()
        val startDate = when (period) {
            "daily" -> today
            "weekly" -> today.minusDays(7)
            "monthly" -> today.minusDays(30)
            else -> LocalDate.of(2000, 1, 1) // "all"
        }

        val rankings = if (period == "all") {
            // Для "all" берем последний рейтинг каждого пользователя
            val allUsers = userRepository.findAll().filter { it.classId == classId && it.role == "ROLE_STUDENT" }
            allUsers.mapNotNull { user ->
                rankingRepository.findByUserIdOrderByDateDesc(user.id!!).firstOrNull()
            }
        } else {
            rankingRepository.findClassRankingsByDate(classId, today)
                .filter { it.date.isAfter(startDate.minusDays(1)) }
        }

        return rankings.sortedByDescending { it.totalPoints }
    }

    fun getGlobalRanking(limit: Int): List<Ranking> {
        val today = LocalDate.now()
        val allUsers = userRepository.findAll().filter { it.role == "ROLE_STUDENT" }
        
        // Получаем последний рейтинг каждого пользователя
        val rankings = allUsers.mapNotNull { user ->
            rankingRepository.findByUserIdOrderByDateDesc(user.id!!).firstOrNull()
        }
        
        return rankings.sortedByDescending { it.totalPoints }
            .take(limit)
            .mapIndexed { index, ranking ->
                ranking.globalRank = index + 1
                ranking
            }
    }

    fun getUserStatistics(userId: Int): UserStatistics {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        val today = LocalDate.now()
        val ranking = rankingRepository.findByUserIdOrderByDateDesc(userId).firstOrNull()
            ?: return UserStatistics.empty(userId, user)

        // Получаем статистику по вызовам (индивидуальные + классовые)
        val individualChallenges = challengeRepository.findByUserIdOrderByDateDesc(userId)
        val classChallenges = if (user.classId != null) {
            challengeRepository.findByClassIdAndDateBetween(
                user.classId,
                LocalDate.of(2000, 1, 1),
                LocalDate.now()
            )
        } else {
            emptyList()
        }
        
        // Объединяем вызовы, убирая дубликаты по дате
        val allChallenges = (individualChallenges + classChallenges)
            .distinctBy { it.date }
            .sortedByDescending { it.date }
        
        val completedChallenges = allChallenges.filter { it.status == "completed" }
        val totalChallenges = allChallenges.size
        val completedCount = completedChallenges.size
        val successRate = if (totalChallenges > 0) (completedCount.toDouble() / totalChallenges) * 100 else 0.0

        // Вычисляем среднее количество попыток
        val attemptsByChallenge = completedChallenges.map { challenge ->
            attemptRepository.findByChallengeIdAndUserId(challenge.id!!, userId).size
        }
        val averageAttempts = if (attemptsByChallenge.isNotEmpty()) {
            attemptsByChallenge.average()
        } else {
            0.0
        }

        // Вычисляем ранги
        val globalRank = ranking.globalRank ?: calculateGlobalRank(userId, today)
        val classRank = if (user.classId != null) {
            ranking.classRank ?: calculateClassRank(userId, user.classId, today)
        } else {
            null
        }

        return UserStatistics(
            userId = userId,
            firstName = user.firstName,
            lastName = user.lastName,
            login = user.login,
            totalPoints = ranking.totalPoints,
            dailyPoints = ranking.dailyPoints,
            currentStreak = ranking.currentStreak,
            longestStreak = ranking.longestStreak,
            globalRank = globalRank,
            classRank = classRank,
            totalChallenges = totalChallenges,
            completedChallenges = completedCount,
            successRate = successRate,
            averageAttempts = averageAttempts,
            lastActivity = ranking.updatedAt
        )
    }

    fun getStreakHistory(userId: Int, days: Int): StreakHistoryResponse {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong())
        
        val rankings = rankingRepository.findByUserIdOrderByDateDesc(userId)
            .filter { it.date.isAfter(startDate.minusDays(1)) && it.date.isBefore(today.plusDays(1)) }
        
        val currentRanking = rankings.firstOrNull()
        val currentStreak = currentRanking?.currentStreak ?: 0
        val longestStreak = rankings.maxOfOrNull { it.longestStreak } ?: 0

        val streakHistory = (0 until days).map { i ->
            val date = today.minusDays(i.toLong())
            val rankingForDate = rankings.find { it.date == date }
            StreakHistoryItem(
                date = date.toString(),
                completed = rankingForDate != null && rankingForDate.dailyPoints > 0
            )
        }.reversed()

        return StreakHistoryResponse(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            streakHistory = streakHistory
        )
    }

    private fun calculateGlobalRank(userId: Int, date: LocalDate): Int {
        val rankings = rankingRepository.findGlobalRankingsByDate(date)
        return rankings.indexOfFirst { it.userId == userId } + 1
    }

    private fun calculateClassRank(userId: Int, classId: Int, date: LocalDate): Int {
        val rankings = rankingRepository.findClassRankingsByDate(classId, date)
        return rankings.indexOfFirst { it.userId == userId } + 1
    }

    fun getUserIdFromAuthentication(authentication: Authentication): Int {
        val username = authentication.name
        val user = userService.findByEmail(username) ?: userService.findByLogin(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        return user.id ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User ID is null")
    }
}

data class UserStatistics(
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
    val lastActivity: LocalDateTime?
) {
    companion object {
        fun empty(userId: Int, user: ru.mirea.wordle.user.model.User): UserStatistics {
            return UserStatistics(
                userId = userId,
                firstName = user.firstName,
                lastName = user.lastName,
                login = user.login,
                totalPoints = 0,
                dailyPoints = 0,
                currentStreak = 0,
                longestStreak = 0,
                globalRank = 0,
                classRank = null,
                totalChallenges = 0,
                completedChallenges = 0,
                successRate = 0.0,
                averageAttempts = 0.0,
                lastActivity = null
            )
        }
    }
}
