package ru.mirea.wordle.task

interface WordCreateStrategy {
    fun newWord(): String
}