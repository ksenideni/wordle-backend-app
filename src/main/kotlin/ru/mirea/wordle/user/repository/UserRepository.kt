package ru.mirea.wordle.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.mirea.wordle.user.model.User
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): Optional<User>
    fun findByLogin(login: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByLogin(login: String): Boolean
}

