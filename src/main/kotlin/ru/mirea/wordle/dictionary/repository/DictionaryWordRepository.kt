package ru.mirea.wordle.dictionary.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.mirea.wordle.dictionary.model.DictionaryWord

@Repository
interface DictionaryWordRepository : JpaRepository<DictionaryWord, Int> {
    fun findByDictionaryId(dictionaryId: Int): List<DictionaryWord>
    fun countByDictionaryId(dictionaryId: Int): Int
    fun deleteByDictionaryIdAndWordIn(dictionaryId: Int, words: List<String>): Int
    fun existsByDictionaryIdAndWord(dictionaryId: Int, word: String): Boolean
    
    @Modifying
    @Query("DELETE FROM DictionaryWord dw WHERE dw.dictionaryId = :dictionaryId")
    fun deleteByDictionaryId(@Param("dictionaryId") dictionaryId: Int): Int
}

