package ru.mirea.wordle.dictionary.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.mirea.wordle.dictionary.model.Dictionary

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Int> {
    fun findByTheme(theme: String): List<Dictionary>
    fun findByCreatedBy(createdBy: Int): List<Dictionary>
}

