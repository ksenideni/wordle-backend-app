package ru.mirea.wordle.auth.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.user.service.UserService
import ru.mirea.wordle.`class`.repository.ClassRepository

data class RegisterTeacherRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)

data class RegisterStudentRequest(
    val login: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val invitationCode: String? = null,
    val classId: Int? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserResponse(
    val id: Int,
    val email: String? = null,
    val login: String? = null,
    val firstName: String,
    val lastName: String,
    val role: String,
    val classId: Int? = null,
    val className: String? = null
)

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val classRepository: ClassRepository
) {

    @PostMapping("/register/teacher")
    fun registerTeacher(@RequestBody request: RegisterTeacherRequest): ResponseEntity<UserResponse> {
        val user = userService.registerTeacher(
            request.email,
            request.firstName,
            request.lastName,
            request.password
        )
        
        // Нормализуем роль (убираем префикс ROLE_)
        val role = when {
            user.role.startsWith("ROLE_") -> user.role.substring(5).lowercase()
            else -> user.role.lowercase()
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            UserResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = role
            )
        )
    }

    @PostMapping("/register/student")
    fun registerStudent(@RequestBody request: RegisterStudentRequest): ResponseEntity<UserResponse> {
        val user = userService.registerStudent(
            request.login,
            request.firstName,
            request.lastName,
            request.password,
            request.invitationCode,
            request.classId
        )
        
        val className = user.classId?.let { classId ->
            classRepository.findById(classId).orElse(null)?.name
        }
        
        // Нормализуем роль (убираем префикс ROLE_)
        val role = when {
            user.role.startsWith("ROLE_") -> user.role.substring(5).lowercase()
            else -> user.role.lowercase()
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            UserResponse(
                id = user.id!!,
                login = user.login,
                firstName = user.firstName,
                lastName = user.lastName,
                role = role,
                classId = user.classId,
                className = className
            )
        )
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        val user = userService.login(request.username, request.password)
        
        // Для студентов получаем информацию о классе
        val className = user.classId?.let { classId ->
            classRepository.findById(classId).orElse(null)?.name
        }
        
        // Нормализуем роль (убираем префикс ROLE_)
        val role = when {
            user.role.startsWith("ROLE_") -> user.role.substring(5).lowercase()
            else -> user.role.lowercase()
        }
        
        return ResponseEntity.ok(
            UserResponse(
                id = user.id!!,
                email = user.email,
                login = user.login,
                firstName = user.firstName,
                lastName = user.lastName,
                role = role,
                classId = user.classId,
                className = className
            )
        )
    }
}

