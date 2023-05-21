package ru.mirea.wordle.storage

import ru.mirea.wordle.game.model.User

interface TargetWordStorage {
    fun getTargetWordForUser(user: User): String
}