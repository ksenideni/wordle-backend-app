package ru.mirea.wordle.auth.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.mirea.wordle.user.model.User
import ru.mirea.wordle.user.service.UserService

@Service
class UserDetailsServiceImpl(private val userService: UserService) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        // Пытаемся найти по email (для учителей)
        val userByEmail = userService.findByEmail(username)
        if (userByEmail != null) {
            return createUserDetails(userByEmail, username)
        }

        // Пытаемся найти по login (для студентов)
        val userByLogin = userService.findByLogin(username)
        if (userByLogin != null) {
            return createUserDetails(userByLogin, username)
        }

        throw UsernameNotFoundException("User not found: $username")
    }

    private fun createUserDetails(user: User, username: String): UserDetails {
        return org.springframework.security.core.userdetails.User(
            username,
            user.passwordHash,
            user.isActive,
            true,
            true,
            true,
            listOf(SimpleGrantedAuthority(user.role))
        )
    }
}

