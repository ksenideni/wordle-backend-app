package ru.mirea.wordle.ranking.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.mirea.wordle.ranking.model.Ranking
import java.time.LocalDate

@Repository
interface RankingRepository : JpaRepository<Ranking, Int> {
    fun findByUserIdAndDate(userId: Int, date: LocalDate): Ranking?
    fun findByUserIdOrderByDateDesc(userId: Int): List<Ranking>
    fun findByClassIdAndDate(classId: Int, date: LocalDate): List<Ranking>
    
    @Query("SELECT r FROM Ranking r WHERE r.userId = :userId AND r.date = :date")
    fun findTodayRanking(@Param("userId") userId: Int, @Param("date") date: LocalDate): Ranking?
    
    @Query("SELECT r FROM Ranking r WHERE r.classId = :classId AND r.date = :date ORDER BY r.totalPoints DESC, r.userId ASC")
    fun findClassRankingsByDate(@Param("classId") classId: Int, @Param("date") date: LocalDate): List<Ranking>
    
    @Query("SELECT r FROM Ranking r WHERE r.date = :date ORDER BY r.totalPoints DESC, r.userId ASC")
    fun findGlobalRankingsByDate(@Param("date") date: LocalDate): List<Ranking>
    
    @Query("SELECT r FROM Ranking r WHERE r.userId = :userId ORDER BY r.date DESC")
    fun findUserRankings(@Param("userId") userId: Int): List<Ranking>
}

