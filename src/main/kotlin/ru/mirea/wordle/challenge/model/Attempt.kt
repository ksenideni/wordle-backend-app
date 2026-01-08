package ru.mirea.wordle.challenge.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "attempts")
class Attempt(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(name = "challenge_id", nullable = false)
    val challengeId: Int,

    @Column(name = "attempt_number", nullable = false)
    val attemptNumber: Int,

    @Column(name = "guessed_word", nullable = false, length = 50)
    val guessedWord: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    val result: String, // JSON string with positions array

    @Column(nullable = false)
    val points: Int = 0,

    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null
)

