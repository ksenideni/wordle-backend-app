package ru.mirea.wordle.dictionary.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "dictionary_words")
class DictionaryWord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "dictionary_id", nullable = false)
    val dictionaryId: Int,

    @Column(nullable = false, length = 50)
    val word: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null
)

