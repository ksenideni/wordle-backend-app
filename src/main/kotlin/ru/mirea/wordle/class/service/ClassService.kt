package ru.mirea.wordle.`class`.service

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import ru.mirea.wordle.`class`.model.Class
import ru.mirea.wordle.`class`.repository.ClassRepository
import ru.mirea.wordle.user.repository.UserRepository
import java.time.LocalDateTime
import java.util.*

@Service
class ClassService(
    private val classRepository: ClassRepository,
    private val userRepository: UserRepository
) {

    fun createClass(name: String, teacherId: Int): Class {
        val now = LocalDateTime.now()
        val invitationCode = generateInvitationCode()
        
        val classEntity = Class(
            teacherId = teacherId,
            name = name,
            invitationCode = invitationCode,
            createdAt = now,
            updatedAt = now
        )

        return classRepository.save(classEntity)
    }

    private fun generateInvitationCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        return (1..6)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    fun getTeacherIdFromAuthentication(authentication: Authentication): Int {
        val username = authentication.name
        val user = userRepository.findByEmail(username)
            .orElseThrow { RuntimeException("User not found") }
        return user.id ?: throw RuntimeException("User ID is null")
    }
}

