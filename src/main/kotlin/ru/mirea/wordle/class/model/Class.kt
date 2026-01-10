package ru.mirea.wordle.`class`.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "classes")
class Class(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "teacher_id", nullable = false)
    val teacherId: Int,

    @Column(nullable = false)
    val name: String,

    @Column(name = "invitation_code", unique = true, nullable = false)
    val invitationCode: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
)

