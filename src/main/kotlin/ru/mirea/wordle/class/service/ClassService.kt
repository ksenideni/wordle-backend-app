package ru.mirea.wordle.`class`.service

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
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

    fun getClassesByTeacherId(teacherId: Int): List<Class> {
        return classRepository.findByTeacherId(teacherId)
    }

    fun getStudentCount(classId: Int): Int {
        return userRepository.countByClassId(classId)
    }

    fun getAllClassesWithTeacherInfo(): List<Pair<Class, String>> {
        val allClasses = classRepository.findAll()
        return allClasses.map { classEntity ->
            val teacher = userRepository.findById(classEntity.teacherId).orElse(null)
            val teacherName = if (teacher != null) {
                "${teacher.firstName} ${teacher.lastName}"
            } else {
                "Неизвестный учитель"
            }
            Pair(classEntity, teacherName)
        }
    }

    fun getClassById(classId: Int, teacherId: Int): Class {
        val classEntity = classRepository.findById(classId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found") }
        
        // Разрешаем просмотр всех классов, но для редактирования/удаления нужны права
        return classEntity
    }
    
    fun getTeacherById(teacherId: Int): ru.mirea.wordle.user.model.User {
        return userRepository.findById(teacherId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found") }
    }
    
    fun isClassOwner(classId: Int, teacherId: Int): Boolean {
        val classEntity = classRepository.findById(classId).orElse(null)
        return classEntity?.teacherId == teacherId
    }

    @Transactional
    fun updateClass(classId: Int, name: String, teacherId: Int): Class {
        if (!isClassOwner(classId, teacherId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this class")
        }
        val classEntity = getClassById(classId, teacherId)
        val updatedClass = Class(
            id = classEntity.id,
            teacherId = classEntity.teacherId,
            name = name,
            invitationCode = classEntity.invitationCode,
            createdAt = classEntity.createdAt,
            updatedAt = LocalDateTime.now()
        )
        return classRepository.save(updatedClass)
    }

    @Transactional
    fun regenerateInvitationCode(classId: Int, teacherId: Int): Class {
        if (!isClassOwner(classId, teacherId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to regenerate code for this class")
        }
        val classEntity = getClassById(classId, teacherId)
        val newCode = generateInvitationCode()
        val updatedClass = Class(
            id = classEntity.id,
            teacherId = classEntity.teacherId,
            name = classEntity.name,
            invitationCode = newCode,
            createdAt = classEntity.createdAt,
            updatedAt = LocalDateTime.now()
        )
        return classRepository.save(updatedClass)
    }

    @Transactional
    fun deleteClass(classId: Int, teacherId: Int) {
        if (!isClassOwner(classId, teacherId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this class")
        }
        
        // Проверяем, есть ли в классе ученики
        val studentCount = getStudentCount(classId)
        if (studentCount > 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Невозможно удалить класс. В классе есть ученики."
            )
        }
        
        val classEntity = getClassById(classId, teacherId)
        classRepository.delete(classEntity)
    }

    fun getClassStudents(classId: Int, teacherId: Int): List<ru.mirea.wordle.user.model.User> {
        // Проверяем, что класс существует
        getClassById(classId, teacherId)
        // Студентов могут просматривать все учителя
        return userRepository.findByClassId(classId)
    }

    @Transactional
    fun removeStudent(classId: Int, studentId: Int, teacherId: Int) {
        if (!isClassOwner(classId, teacherId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to remove students from this class")
        }
        getClassById(classId, teacherId) // Проверяем, что класс существует
        val student = userRepository.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found") }
        
        if (student.classId != classId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Student does not belong to this class")
        }
        
        // Создаем новый объект User с обновленным classId = null
        val updatedStudent = ru.mirea.wordle.user.model.User(
            id = student.id,
            email = student.email,
            login = student.login,
            firstName = student.firstName,
            lastName = student.lastName,
            role = student.role,
            passwordHash = student.passwordHash,
            classId = null,
            registrationDate = student.registrationDate,
            isActive = student.isActive,
            createdAt = student.createdAt,
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(updatedStudent)
    }
}

