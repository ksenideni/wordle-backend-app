package ru.mirea.wordle.challenge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.mirea.wordle.challenge.model.Attempt

@Repository
interface StudentAttemptRepository : JpaRepository<Attempt, Int> {
    fun findByChallengeIdOrderByAttemptNumberAsc(challengeId: Int): List<Attempt>
    fun findByChallengeIdAndUserId(challengeId: Int, userId: Int): List<Attempt>
    fun countByChallengeIdAndUserId(challengeId: Int, userId: Int): Int
    fun existsByChallengeIdAndUserIdAndAttemptNumber(challengeId: Int, userId: Int, attemptNumber: Int): Boolean
}

