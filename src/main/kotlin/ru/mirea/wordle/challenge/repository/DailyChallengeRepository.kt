package ru.mirea.wordle.challenge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.mirea.wordle.challenge.model.DailyChallenge
import java.time.LocalDate

@Repository
interface DailyChallengeRepository : JpaRepository<DailyChallenge, Int> {
    fun findByDateAndUserId(date: LocalDate, userId: Int): DailyChallenge?
    fun findByDateAndClassIdAndUserIdIsNull(date: LocalDate, classId: Int): DailyChallenge?
    fun findByUserIdOrderByDateDesc(userId: Int): List<DailyChallenge>
    
    @Query("SELECT dc FROM DailyChallenge dc WHERE dc.userId = :userId AND dc.date BETWEEN :startDate AND :endDate ORDER BY dc.date DESC")
    fun findByUserIdAndDateBetween(
        @Param("userId") userId: Int,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<DailyChallenge>
    
    @Query("SELECT dc FROM DailyChallenge dc WHERE dc.classId = :classId AND dc.userId IS NULL AND dc.date BETWEEN :startDate AND :endDate ORDER BY dc.date DESC")
    fun findByClassIdAndDateBetween(
        @Param("classId") classId: Int,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<DailyChallenge>
}

