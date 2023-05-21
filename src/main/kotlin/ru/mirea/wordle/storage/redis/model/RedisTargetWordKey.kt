package ru.mirea.wordle.storage.redis.model

data class RedisTargetWordKey(val chatId: String) {
    override fun toString(): String {
        return "$DAILY_WORD_PREFIX$chatId"
    }

    companion object {
        const val DAILY_WORD_PREFIX = "word:"
    }

}
