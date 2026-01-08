package ru.mirea.wordle.config.web

data class ErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

