package ru.mirea.wordle.`class`.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.mirea.wordle.`class`.service.ClassService

data class CreateClassRequest(
    val name: String
)

data class ClassResponse(
    val id: Int,
    val name: String,
    val teacherId: Int,
    val invitationCode: String,
    val activeDictionaryId: Int? = null,
    val createdAt: String? = null
)

data class ClassListItemResponse(
    val id: Int,
    val name: String,
    val invitationCode: String,
    val studentCount: Int,
    val activeDictionaryId: Int? = null,
    val createdAt: String? = null
)

data class ClassesListResponse(
    val classes: List<ClassListItemResponse>
)

@RestController
@RequestMapping("/classes")
class ClassController(
    private val classService: ClassService
) {

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    fun createClass(
        @RequestBody request: CreateClassRequest,
        authentication: Authentication
    ): ResponseEntity<ClassResponse> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        val classEntity = classService.createClass(request.name, teacherId)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ClassResponse(
                id = classEntity.id!!,
                name = classEntity.name,
                teacherId = classEntity.teacherId,
                invitationCode = classEntity.invitationCode,
                activeDictionaryId = classEntity.activeDictionaryId,
                createdAt = classEntity.createdAt?.toString()
            )
        )
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    fun getClasses(authentication: Authentication): ResponseEntity<ClassesListResponse> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        val classes = classService.getClassesByTeacherId(teacherId)
        
        val classesResponse = classes.map { classEntity ->
            val studentCount = classService.getStudentCount(classEntity.id!!)
            ClassListItemResponse(
                id = classEntity.id,
                name = classEntity.name,
                invitationCode = classEntity.invitationCode,
                studentCount = studentCount,
                activeDictionaryId = classEntity.activeDictionaryId,
                createdAt = classEntity.createdAt?.toString()
            )
        }
        
        return ResponseEntity.ok(ClassesListResponse(classes = classesResponse))
    }
}

