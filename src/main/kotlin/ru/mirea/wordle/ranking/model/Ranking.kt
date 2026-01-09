package ru.mirea.wordle.ranking.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "rankings")
class Ranking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(name = "class_id", nullable = true)
    val classId: Int? = null,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "daily_points", nullable = false)
    var dailyPoints: Int = 0,

    @Column(name = "total_points", nullable = false)
    var totalPoints: Int = 0,

    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int = 0,

    @Column(name = "longest_streak", nullable = false)
    var longestStreak: Int = 0,

    @Column(name = "global_rank", nullable = true)
    var globalRank: Int? = null,

    @Column(name = "class_rank", nullable = true)
    var classRank: Int? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

