package ru.mirea.wordle.game.service

import org.springframework.stereotype.Service
import ru.mirea.wordle.game.exception.AlreadyLostException
import ru.mirea.wordle.game.exception.AlreadyWonException
import ru.mirea.wordle.game.model.Color
import ru.mirea.wordle.game.model.GuessResult
import ru.mirea.wordle.game.model.GuessResult.Letter
import ru.mirea.wordle.game.model.Progress

@Service
class WordleServiceImpl : WordleService {

    override fun makeTry(progress: Progress, currentWord: String, targetWord: String): Progress {
        validateUserState(progress)
        validateLength(currentWord, targetWord)
        val letters = currentWord.chars()
            .mapToObj { character: Int -> Letter(character.toChar(), null) }
            .toList()
        val targetWordMap: MutableMap<Int, Char> = HashMap()
        for (i in targetWord.indices) {
            targetWordMap[i] = targetWord[i]
        }
        markGreenLetters(letters, targetWordMap)
        val targetWordSet: MutableSet<Char> = HashSet(targetWordMap.values)
        markYellowLetters(letters, targetWordSet)
        markGreyLetters(letters)
        val tries: MutableList<GuessResult> = ArrayList(progress.tries)
        tries.add(GuessResult(letters))
        return Progress(currentWord == targetWord, tries)
    }

    private fun validateUserState(progress: Progress) {
        if (progress.won) {
            throw AlreadyWonException()
        }
        if (progress.tries.size >= Companion.MAX_TRIES_COUNT) {
            throw AlreadyLostException()
        }
    }

    private fun validateLength(currentWord: String, targetWord: String) {
        require(currentWord.length == targetWord.length) { "words length must be the same" }
    }

    private fun markGreenLetters(letters: List<Letter>, targetWordMap: MutableMap<Int, Char>) {
        for (i in letters.indices) {
            val currentLetter = letters[i]
            val currentChar = currentLetter.character
            val targetChar = targetWordMap[i]!!
            if (targetChar == currentChar) {
                currentLetter.color = Color.GREEN
                targetWordMap.remove(i)
            }
        }
    }

    private fun markYellowLetters(letters: List<Letter>, targetWordSet: MutableSet<Char>) {
        for (currentLetter in letters) {
            if (currentLetter.color != null) {
                continue
            }
            val currentChar = currentLetter.character
            if (targetWordSet.contains(currentChar)) {
                currentLetter.color = Color.YELLOW
                targetWordSet.remove(currentChar)
            }
        }
    }

    private fun markGreyLetters(letters: List<Letter>) {
        for (currentLetter in letters) {
            if (currentLetter.color != null) {
                continue
            }
            currentLetter.color = Color.GREY
        }
    }

    companion object {
        private const val MAX_TRIES_COUNT = 5;
    }


}
