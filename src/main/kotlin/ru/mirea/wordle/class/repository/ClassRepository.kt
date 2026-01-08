package ru.mirea.wordle.`class`.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.mirea.wordle.`class`.model.Class
import java.util.*

@Repository
interface ClassRepository : JpaRepository<Class, Int> {
    fun findByInvitationCode(invitationCode: String): Optional<Class>
}

