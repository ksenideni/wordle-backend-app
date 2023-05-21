package ru.mirea.wordle.telegram

class GetScoreException(chatId: String, userId: String):
        RuntimeException("Could not get score for user with id $userId in chat $chatId")