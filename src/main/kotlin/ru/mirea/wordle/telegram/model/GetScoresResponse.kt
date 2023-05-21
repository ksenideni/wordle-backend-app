package ru.mirea.wordle.telegram.model

data class GetScoresResponse(
        val ok: Boolean,
        val result: List<Score>,
)

data class Score(
        val user: User,
        val score: Int,
)

data class User(
        val id: Int,
)