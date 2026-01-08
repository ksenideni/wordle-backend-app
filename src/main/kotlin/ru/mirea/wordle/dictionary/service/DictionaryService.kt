package ru.mirea.wordle.dictionary.service

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.mirea.wordle.user.service.UserService
import ru.mirea.wordle.dictionary.model.Dictionary
import ru.mirea.wordle.dictionary.model.DictionaryWord
import ru.mirea.wordle.dictionary.repository.DictionaryRepository
import ru.mirea.wordle.dictionary.repository.DictionaryWordRepository
import java.time.LocalDateTime

@Service
class DictionaryService(
    private val dictionaryRepository: DictionaryRepository,
    private val dictionaryWordRepository: DictionaryWordRepository,
    private val userService: ru.mirea.wordle.user.service.UserService
) {

    @Transactional
    fun createDictionary(
        name: String,
        theme: String?,
        words: List<String>,
        teacherId: Int
    ): Dictionary {
        val now = LocalDateTime.now()
        val dictionary = Dictionary(
            name = name,
            theme = theme,
            createdBy = teacherId,
            createdAt = now,
            updatedAt = now
        )

        val savedDictionary = dictionaryRepository.save(dictionary)

        // Валидация и сохранение слов
        val validWords = words.filter { it.length == 5 && it.all { char -> char.isLetter() } }
            .map { it.uppercase() }
            .distinct()

        if (validWords.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid words provided. Words must be 5 letters long")
        }

        val dictionaryWords = validWords.map { word ->
            DictionaryWord(
                dictionaryId = savedDictionary.id!!,
                word = word,
                createdAt = now
            )
        }

        dictionaryWordRepository.saveAll(dictionaryWords)

        return savedDictionary
    }

    fun getDictionaries(theme: String?): List<Dictionary> {
        return if (theme != null) {
            dictionaryRepository.findByTheme(theme)
        } else {
            dictionaryRepository.findAll()
        }
    }

    fun getDictionaryById(id: Int): Dictionary {
        return dictionaryRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Dictionary not found") }
    }

    fun getWordsByDictionaryId(dictionaryId: Int): List<String> {
        return dictionaryWordRepository.findByDictionaryId(dictionaryId)
            .map { it.word }
    }

    fun getWordCount(dictionaryId: Int): Int {
        return dictionaryWordRepository.countByDictionaryId(dictionaryId)
    }

    @Transactional
    fun replaceWords(dictionaryId: Int, words: List<String>, teacherId: Int): Int {
        val dictionary = getDictionaryById(dictionaryId)
        
        // Проверка прав доступа
        if (dictionary.createdBy != teacherId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this dictionary")
        }

        // Валидация слов
        val validWords = words.filter { it.length == 5 && it.all { char -> char.isLetter() } }
            .map { it.uppercase() }
            .distinct()

        if (validWords.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid words provided. Words must be 5 letters long")
        }

        val now = LocalDateTime.now()

        // Получаем текущие слова из БД
        val existingWords = dictionaryWordRepository.findByDictionaryId(dictionaryId)
        val existingWordsSet = existingWords.map { it.word }.toSet()
        val newWordsSet = validWords.toSet()

        // Находим слова для удаления (есть в БД, но нет в новом списке)
        val wordsToDelete = existingWordsSet - newWordsSet
        if (wordsToDelete.isNotEmpty()) {
            dictionaryWordRepository.deleteByDictionaryIdAndWordIn(dictionaryId, wordsToDelete.toList())
        }

        // Находим слова для добавления (есть в новом списке, но нет в БД)
        val wordsToAdd = newWordsSet - existingWordsSet
        if (wordsToAdd.isNotEmpty()) {
            val newDictionaryWords = wordsToAdd.map { word ->
                DictionaryWord(
                    dictionaryId = dictionaryId,
                    word = word,
                    createdAt = now
                )
            }
            dictionaryWordRepository.saveAll(newDictionaryWords)
        }

        // Обновляем updated_at словаря только если были изменения
        if (wordsToDelete.isNotEmpty() || wordsToAdd.isNotEmpty()) {
            dictionary.updatedAt = now
            dictionaryRepository.save(dictionary)
        }

        return wordsToAdd.size
    }

    @Transactional
    fun deleteDictionary(dictionaryId: Int, teacherId: Int) {
        val dictionary = getDictionaryById(dictionaryId)
        
        // Проверка прав доступа
        if (dictionary.createdBy != teacherId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this dictionary")
        }

        dictionaryRepository.delete(dictionary)
    }

    fun getTeacherIdFromAuthentication(authentication: Authentication): Int {
        val username = authentication.name
        val user = userService.findByEmail(username)
            ?: throw RuntimeException("User not found")
        return user.id ?: throw RuntimeException("User ID is null")
    }
}

