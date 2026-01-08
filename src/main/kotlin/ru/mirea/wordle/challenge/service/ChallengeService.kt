package ru.mirea.wordle.challenge.service

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.mirea.wordle.challenge.model.DailyChallenge
import ru.mirea.wordle.challenge.repository.DailyChallengeRepository
import ru.mirea.wordle.`class`.repository.ClassRepository
import ru.mirea.wordle.dictionary.repository.DictionaryWordRepository
import ru.mirea.wordle.user.repository.UserRepository
import ru.mirea.wordle.user.service.UserService
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ChallengeService(
    private val challengeRepository: DailyChallengeRepository,
    private val dictionaryWordRepository: DictionaryWordRepository,
    private val classRepository: ClassRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
) {

    fun getTodayChallenge(userId: Int): DailyChallenge {
        val today = LocalDate.now()
        // Сначала ищем индивидуальный вызов
        val individualChallenge = challengeRepository.findByDateAndUserId(today, userId)
        if (individualChallenge != null) {
            return individualChallenge
        }
        
        // Если индивидуального нет, ищем вызов класса
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        
        if (user.classId != null) {
            val classChallenge = challengeRepository.findByDateAndClassIdAndUserIdIsNull(today, user.classId)
            if (classChallenge != null) {
                return classChallenge
            }
        }
        
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found for today")
    }

    @Transactional
    fun createClassChallenge(
        classId: Int,
        date: LocalDate?,
        dictionaryId: Int?,
        word: String?,
        teacherId: Int
    ): DailyChallenge {
        val classEntity = classRepository.findById(classId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found") }

        if (classEntity.teacherId != teacherId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to create challenges for this class")
        }

        val challengeDate = date ?: LocalDate.now()
        val finalDictionaryId = dictionaryId ?: classEntity.activeDictionaryId
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary ID is required")

        // Проверяем, не существует ли уже вызов для класса на эту дату
        val existingChallenge = challengeRepository.findByDateAndClassIdAndUserIdIsNull(challengeDate, classId)
        if (existingChallenge != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Challenge already exists for this class on this date")
        }

        val finalWord = (word?.uppercase()) ?: getRandomWordFromDictionary(finalDictionaryId)

        val now = LocalDateTime.now()
        val challenge = DailyChallenge(
            date = challengeDate,
            word = finalWord,
            dictionaryId = finalDictionaryId,
            classId = classId,
            userId = null,
            status = "active",
            createdAt = now,
            updatedAt = now
        )

        return challengeRepository.save(challenge)
    }

    @Transactional
    fun createStudentChallenge(
        studentId: Int,
        date: LocalDate?,
        dictionaryId: Int,
        word: String?,
        teacherId: Int
    ): DailyChallenge {
        val student = userRepository.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found") }

        if (student.role != "ROLE_STUDENT") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a student")
        }

        // Проверяем, что учитель имеет доступ к классу студента
        if (student.classId != null) {
            val classEntity = classRepository.findById(student.classId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found") }
            if (classEntity.teacherId != teacherId) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to create challenges for this student")
            }
        }

        val challengeDate = date ?: LocalDate.now()

        // Проверяем, не существует ли уже вызов для студента на эту дату
        val existingChallenge = challengeRepository.findByDateAndUserId(challengeDate, studentId)
        if (existingChallenge != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Challenge already exists for this student on this date")
        }

        val finalWord = (word?.uppercase()) ?: getRandomWordFromDictionary(dictionaryId)

        val now = LocalDateTime.now()
        val challenge = DailyChallenge(
            date = challengeDate,
            word = finalWord,
            dictionaryId = dictionaryId,
            classId = null,
            userId = studentId,
            status = "active",
            createdAt = now,
            updatedAt = now
        )

        return challengeRepository.save(challenge)
    }

    @Transactional
    fun createIndividualChallengesForClass(
        classId: Int,
        date: LocalDate?,
        dictionaryId: Int,
        teacherId: Int
    ): List<DailyChallenge> {
        val classEntity = classRepository.findById(classId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found") }

        if (classEntity.teacherId != teacherId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to create challenges for this class")
        }

        // Получаем всех студентов класса
        val allUsers = userRepository.findAll()
        val students = allUsers.filter { 
            it.classId == classId && it.role == "ROLE_STUDENT" && it.id != null 
        }
        if (students.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No students found in this class")
        }

        val challengeDate = date ?: LocalDate.now()
        val words = dictionaryWordRepository.findByDictionaryId(dictionaryId)
        if (words.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary is empty")
        }

        val now = LocalDateTime.now()
        val challenges = mutableListOf<DailyChallenge>()

        for (student in students) {
            // Проверяем, не существует ли уже вызов для студента на эту дату
            val existingChallenge = challengeRepository.findByDateAndUserId(challengeDate, student.id!!)
            if (existingChallenge != null) {
                continue // Пропускаем, если уже есть вызов
            }

            val randomWord = words.random().word
            val challenge = DailyChallenge(
                date = challengeDate,
                word = randomWord,
                dictionaryId = dictionaryId,
                classId = null,
                userId = student.id,
                status = "active",
                createdAt = now,
                updatedAt = now
            )
            challenges.add(challengeRepository.save(challenge))
        }

        return challenges
    }

    fun getChallengeHistory(
        userId: Int,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int
    ): List<DailyChallenge> {
        val start = startDate ?: LocalDate.now().minusDays(30)
        val end = endDate ?: LocalDate.now()

        return challengeRepository.findByUserIdAndDateBetween(userId, start, end)
            .take(limit)
    }

    private fun getRandomWordFromDictionary(dictionaryId: Int): String {
        val words = dictionaryWordRepository.findByDictionaryId(dictionaryId)
        if (words.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary is empty")
        }
        return words.random().word
    }

    fun getTeacherIdFromAuthentication(authentication: Authentication): Int {
        val username = authentication.name
        val user = userService.findByEmail(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teacher not found")
        return user.id ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Teacher ID is null")
    }

    fun getUserIdFromAuthentication(authentication: Authentication): Int {
        val username = authentication.name
        val user = userService.findByEmail(username) ?: userService.findByLogin(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        return user.id ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User ID is null")
    }
}

