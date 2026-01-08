package ru.mirea.wordle.user.service

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.mirea.wordle.`class`.repository.ClassRepository
import ru.mirea.wordle.user.model.User
import ru.mirea.wordle.user.repository.UserRepository
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val classRepository: ClassRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun registerTeacher(email: String, firstName: String, lastName: String, password: String): User {
        if (userRepository.existsByEmail(email)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists")
        }

        val passwordHash = passwordEncoder.encode(password)
        val now = LocalDateTime.now()
        val user = User(
            email = email,
            firstName = firstName,
            lastName = lastName,
            role = "ROLE_TEACHER",
            passwordHash = passwordHash,
            registrationDate = now,
            createdAt = now,
            updatedAt = now
        )

        return userRepository.save(user)
    }

    fun registerStudent(
        login: String,
        firstName: String,
        lastName: String,
        password: String,
        invitationCode: String?,
        classId: Int?
    ): User {
        if (userRepository.existsByLogin(login)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Login already exists")
        }

        if (invitationCode == null && classId == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Either invitationCode or classId must be provided")
        }

        val finalClassId: Int = when {
            classId != null -> {
                if (!classRepository.existsById(classId)) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class with id $classId not found")
                }
                classId
            }
            invitationCode != null -> {
                val classEntity = classRepository.findByInvitationCode(invitationCode)
                    .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code") }
                classEntity.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class ID is null")
            }
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Either invitationCode or classId must be provided")
        }

        val passwordHash = passwordEncoder.encode(password)
        val now = LocalDateTime.now()
        val user = User(
            login = login,
            firstName = firstName,
            lastName = lastName,
            role = "ROLE_STUDENT",
            passwordHash = passwordHash,
            classId = finalClassId,
            registrationDate = now,
            createdAt = now,
            updatedAt = now
        )

        return userRepository.save(user)
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email).orElse(null)
    }

    fun findByLogin(login: String): User? {
        return userRepository.findByLogin(login).orElse(null)
    }
}

