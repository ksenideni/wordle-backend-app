package ru.mirea.wordle.dictionary.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.dictionary.service.DictionaryService

data class CreateDictionaryRequest(
    val name: String,
    val theme: String? = null,
    val words: List<String> = emptyList()
)

data class DictionaryResponse(
    val id: Int,
    val name: String,
    val theme: String? = null,
    val createdBy: Int? = null,
    val wordCount: Int,
    val createdAt: String? = null
)

data class DictionaryWithWordsResponse(
    val id: Int,
    val name: String,
    val theme: String? = null,
    val words: List<String>,
    val wordCount: Int,
    val createdAt: String? = null
)

data class DictionaryListItemResponse(
    val id: Int,
    val name: String,
    val theme: String? = null,
    val wordCount: Int,
    val createdBy: Int? = null
)

data class DictionariesListResponse(
    val dictionaries: List<DictionaryListItemResponse>
)

data class ReplaceWordsRequest(
    val words: List<String>
)

data class ReplaceWordsResponse(
    val totalWords: Int
)

@RestController
@RequestMapping("/dictionaries")
class DictionaryController(
    private val dictionaryService: DictionaryService
) {

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    fun createDictionary(
        @RequestBody request: CreateDictionaryRequest,
        authentication: Authentication
    ): ResponseEntity<DictionaryResponse> {
        val teacherId = dictionaryService.getTeacherIdFromAuthentication(authentication)
        val dictionary = dictionaryService.createDictionary(
            name = request.name,
            theme = request.theme,
            words = request.words,
            teacherId = teacherId
        )
        val wordCount = dictionaryService.getWordCount(dictionary.id!!)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            DictionaryResponse(
                id = dictionary.id,
                name = dictionary.name,
                theme = dictionary.theme,
                createdBy = dictionary.createdBy,
                wordCount = wordCount,
                createdAt = dictionary.createdAt?.toString()
            )
        )
    }

    @GetMapping
    fun getDictionaries(
        @RequestParam(required = false) theme: String?,
        authentication: Authentication?
    ): ResponseEntity<DictionariesListResponse> {
        val dictionaries = dictionaryService.getDictionaries(theme)
        
        val dictionariesResponse = dictionaries.map { dictionary ->
            val wordCount = dictionaryService.getWordCount(dictionary.id!!)
            DictionaryListItemResponse(
                id = dictionary.id,
                name = dictionary.name,
                theme = dictionary.theme,
                wordCount = wordCount,
                createdBy = dictionary.createdBy
            )
        }

        return ResponseEntity.ok(DictionariesListResponse(dictionaries = dictionariesResponse))
    }

    @GetMapping("/{dictionaryId}")
    fun getDictionary(@PathVariable dictionaryId: Int): ResponseEntity<DictionaryWithWordsResponse> {
        val dictionary = dictionaryService.getDictionaryById(dictionaryId)
        val words = dictionaryService.getWordsByDictionaryId(dictionaryId)
        val wordCount = dictionaryService.getWordCount(dictionaryId)

        return ResponseEntity.ok(
            DictionaryWithWordsResponse(
                id = dictionary.id!!,
                name = dictionary.name,
                theme = dictionary.theme,
                words = words,
                wordCount = wordCount,
                createdAt = dictionary.createdAt?.toString()
            )
        )
    }

    @PostMapping("/{dictionaryId}/words")
    @PreAuthorize("hasRole('TEACHER')")
    fun replaceWords(
        @PathVariable dictionaryId: Int,
        @RequestBody request: ReplaceWordsRequest,
        authentication: Authentication
    ): ResponseEntity<ReplaceWordsResponse> {
        val teacherId = dictionaryService.getTeacherIdFromAuthentication(authentication)
        dictionaryService.replaceWords(dictionaryId, request.words, teacherId)
        val totalWords = dictionaryService.getWordCount(dictionaryId)

        return ResponseEntity.ok(
            ReplaceWordsResponse(
                totalWords = totalWords
            )
        )
    }

    @DeleteMapping("/{dictionaryId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun deleteDictionary(
        @PathVariable dictionaryId: Int,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val teacherId = dictionaryService.getTeacherIdFromAuthentication(authentication)
        dictionaryService.deleteDictionary(dictionaryId, teacherId)
        return ResponseEntity.noContent().build()
    }
}

