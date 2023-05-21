package ru.mirea.wordle.storage

/**
 * Интерфейс, предназначенный для обновления слова дня и прогресса пользователей чата.
 *
 * */
interface WordRefresher {
    /**
     * Метод ддя обновления слова дня и обнуления прогресса пользователям чата.
     * Является атомарной операцией.
     *
     * @param chatId идентификатор чата
     * @param newWord новое слово дня
     */
    fun updateWordAndRefreshUsersProgresses(chatId: String, newWord: String)
}