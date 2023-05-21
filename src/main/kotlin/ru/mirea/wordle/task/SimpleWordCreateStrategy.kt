package ru.mirea.wordle.task

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class SimpleWordCreateStrategy : WordCreateStrategy {
    override fun newWord(): String {
        return WORDS.get(Random.nextInt(WORDS.size))
    }

    companion object {
        val WORDS: Array<String> = arrayOf(
            "fruit",
            "juice",
            "brave",
            "fight"
        )
    }
}