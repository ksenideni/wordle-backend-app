package ru.mirea.wordle.`class`.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ru.mirea.wordle.`class`.service.ClassService

data class CreateClassRequest(
    val name: String
)

data class ClassResponse(
    val id: Int,
    val name: String,
    val teacherId: Int,
    val invitationCode: String,
    val createdAt: String? = null
)

data class ClassListItemResponse(
    val id: Int,
    val name: String,
    val teacherName: String,
    val studentCount: Int,
    val isMine: Boolean = false,
    val createdAt: String? = null
)

data class ClassesListResponse(
    val classes: List<ClassListItemResponse>
)

data class PublicClassItemResponse(
    val id: Int,
    val name: String,
    val teacherName: String
)

data class PublicClassesListResponse(
    val classes: List<PublicClassItemResponse>
)

data class UpdateClassRequest(
    val name: String
)

data class ClassWithStudentsResponse(
    val id: Int,
    val name: String,
    val teacherId: Int,
    val teacherName: String,
    val isMine: Boolean = false,
    val students: List<StudentItemResponse>,
    val createdAt: String? = null
)

data class StudentItemResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val login: String?,
    val email: String?,
    val registrationDate: String? = null
)

data class StudentsListResponse(
    val students: List<StudentItemResponse>
)

data class RegenerateCodeResponse(
    val invitationCode: String
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
                createdAt = classEntity.createdAt?.toString()
            )
        )
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    fun getClasses(authentication: Authentication): ResponseEntity<ClassesListResponse> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        val allClassesWithTeachers = classService.getAllClassesWithTeacherInfo()
        
        val classesResponse = allClassesWithTeachers.map { (classEntity, teacherName) ->
            val studentCount = classService.getStudentCount(classEntity.id!!)
            val isMine = classEntity.teacherId == teacherId
            ClassListItemResponse(
                id = classEntity.id,
                name = classEntity.name,
                teacherName = teacherName,
                studentCount = studentCount,
                isMine = isMine,
                createdAt = classEntity.createdAt?.toString()
            )
        }
        
        return ResponseEntity.ok(ClassesListResponse(classes = classesResponse))
    }

    @GetMapping("/public")
    fun getPublicClasses(): ResponseEntity<PublicClassesListResponse> {
        val classesWithTeachers = classService.getAllClassesWithTeacherInfo()
        
        val classesResponse = classesWithTeachers.map { (classEntity, teacherName) ->
            PublicClassItemResponse(
                id = classEntity.id!!,
                name = classEntity.name,
                teacherName = teacherName
            )
        }
        
        return ResponseEntity.ok(PublicClassesListResponse(classes = classesResponse))
    }

    @GetMapping("/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun getClass(
        @PathVariable classId: Int,
        authentication: Authentication
    ): ResponseEntity<ClassWithStudentsResponse> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        val classEntity = classService.getClassById(classId, teacherId)
        val isMine = classService.isClassOwner(classId, teacherId)
        
        // Получаем имя учителя
        val teacher = classService.getTeacherById(classEntity.teacherId)
        val teacherName = "${teacher.firstName} ${teacher.lastName}"
        
        // Студентов показываем для всех классов (но редактировать могут только владельцы)
        val students = classService.getClassStudents(classId, teacherId)
        
        val studentsResponse = students.map { student ->
            StudentItemResponse(
                id = student.id!!,
                firstName = student.firstName,
                lastName = student.lastName,
                login = student.login,
                email = student.email,
                registrationDate = student.registrationDate?.toString()
            )
        }
        
        return ResponseEntity.ok(
            ClassWithStudentsResponse(
                id = classEntity.id!!,
                name = classEntity.name,
                teacherId = classEntity.teacherId,
                teacherName = teacherName,
                isMine = isMine,
                students = studentsResponse,
                createdAt = classEntity.createdAt?.toString()
            )
        )
    }

    @PutMapping("/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun updateClass(
        @PathVariable classId: Int,
        @RequestBody request: UpdateClassRequest,
        authentication: Authentication
    ): ResponseEntity<ClassResponse> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        val classEntity = classService.updateClass(classId, request.name, teacherId)
        
        return ResponseEntity.ok(
            ClassResponse(
                id = classEntity.id!!,
                name = classEntity.name,
                teacherId = classEntity.teacherId,
                invitationCode = classEntity.invitationCode,
                createdAt = classEntity.createdAt?.toString()
            )
        )
    }

    @DeleteMapping("/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun deleteClass(
        @PathVariable classId: Int,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        classService.deleteClass(classId, teacherId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{classId}/regenerate-code")
    @PreAuthorize("hasRole('TEACHER')")
    fun regenerateInvitationCode(
        @PathVariable classId: Int,
        authentication: Authentication
    ): ResponseEntity<RegenerateCodeResponse> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        val classEntity = classService.regenerateInvitationCode(classId, teacherId)
        
        return ResponseEntity.ok(
            RegenerateCodeResponse(invitationCode = classEntity.invitationCode)
        )
    }

    @GetMapping("/{classId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    fun getClassStudents(
        @PathVariable classId: Int,
        authentication: Authentication
    ): ResponseEntity<StudentsListResponse> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        
        // Студентов теперь может просматривать любой учитель
        val students = classService.getClassStudents(classId, teacherId)
        
        val studentsResponse = students.map { student ->
            StudentItemResponse(
                id = student.id!!,
                firstName = student.firstName,
                lastName = student.lastName,
                login = student.login,
                email = student.email,
                registrationDate = student.registrationDate?.toString()
            )
        }
        
        return ResponseEntity.ok(StudentsListResponse(students = studentsResponse))
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    fun removeStudent(
        @PathVariable classId: Int,
        @PathVariable studentId: Int,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val teacherId = classService.getTeacherIdFromAuthentication(authentication)
        classService.removeStudent(classId, studentId, teacherId)
        return ResponseEntity.noContent().build()
    }
}

