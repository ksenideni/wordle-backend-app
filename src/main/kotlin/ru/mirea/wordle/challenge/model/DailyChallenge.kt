package ru.mirea.wordle.challenge.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "daily_challenges")
class DailyChallenge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(nullable = false, length = 50)
    val word: String,

    @Column(name = "class_id", nullable = true)
    val classId: Int? = null,

    @Column(name = "user_id", nullable = true)
    val userId: Int? = null,

    @Column(nullable = false, length = 20)
    var status: String = "active",

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

