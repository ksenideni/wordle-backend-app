package ru.mirea.wordle.game.model


data class GuessResult(val letters: List<Letter>) {
    class Letter(var character: Char, var color: Color?) {
    }
}
