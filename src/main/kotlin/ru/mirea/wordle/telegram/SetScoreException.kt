package ru.mirea.wordle.telegram

class SetScoreException(chatId: String, userId: String):
        RuntimeException("Could not set score for user with id $userId in chat $chatId")