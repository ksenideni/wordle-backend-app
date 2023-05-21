package ru.mirea.wordle.storage.redis.model

data class RedisUserKey(val chatId: String, val userId: String) {

    override fun toString(): String {
        return "$chatId:$userId"
    }

    companion object {
        fun chatMembersWildcard(chatId: String): String {
            return "$chatId:*"
        }
    }

}
