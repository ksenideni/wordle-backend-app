package ru.mirea.wordle.user.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(unique = true, nullable = true)
    val email: String? = null,

    @Column(unique = true, nullable = true)
    val login: String? = null,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(nullable = false)
    val role: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "class_id", nullable = true)
    val classId: Int? = null,

    @Column(name = "registration_date")
    val registrationDate: LocalDateTime? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
)

