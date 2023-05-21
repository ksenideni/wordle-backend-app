package ru.mirea.wordle.storage.redis.exception

class WordNotFoundException(val chatId: String) : RuntimeException("Word for chat $chatId not found") {
}