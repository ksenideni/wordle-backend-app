package ru.mirea.wordle.auth.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.user.service.UserService

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
    val invitationCode: String
)

data class UserResponse(
    val id: Int,
    val email: String? = null,
    val login: String? = null,
    val firstName: String,
    val lastName: String,
    val role: String
)

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
) {

    @PostMapping("/register/teacher")
    fun registerTeacher(@RequestBody request: RegisterTeacherRequest): ResponseEntity<UserResponse> {
        return try {
            val user = userService.registerTeacher(
                request.email,
                request.firstName,
                request.lastName,
                request.password
            )
            ResponseEntity.status(HttpStatus.CREATED).body(
                UserResponse(
                    id = user.id!!,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    role = user.role
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PostMapping("/register/student")
    fun registerStudent(@RequestBody request: RegisterStudentRequest): ResponseEntity<UserResponse> {
        return try {
            val user = userService.registerStudent(
                request.login,
                request.firstName,
                request.lastName,
                request.password,
                request.invitationCode
            )
            ResponseEntity.status(HttpStatus.CREATED).body(
                UserResponse(
                    id = user.id!!,
                    login = user.login,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    role = user.role
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }
}

