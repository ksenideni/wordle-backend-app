package ru.mirea.wordle.dictionary.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "dictionaries")
class Dictionary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = true)
    val theme: String? = null,

    @Column(name = "created_by", nullable = true)
    val createdBy: Int? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

