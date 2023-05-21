package ru.mirea.wordle.task

import io.lettuce.core.ScanArgs
import io.lettuce.core.api.StatefulRedisConnection
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import ru.mirea.wordle.storage.WordRefresher
import ru.mirea.wordle.storage.redis.model.RedisTargetWordKey

@DisallowConcurrentExecution
class UpdateWordJob(
    private var wordRefresher: WordRefresher,
    private var wordCreateStrategy: WordCreateStrategy,
    private var userStorageConnection: StatefulRedisConnection<String, String>,
    @Value("\${task.update.batch.size:1}")
    private var chatsBatchSize: Int
) : Job {

    override fun execute(context: JobExecutionContext) {
        LOG.info("Starting update job")
        val scanArgs = ScanArgs.Builder
            .limit(chatsBatchSize.toLong())
            .match(RedisTargetWordKey.DAILY_WORD_PREFIX + '*')
        var cursor = userStorageConnection.sync().scan(scanArgs)
        while (true) {
            cursor.keys.stream()
                .map { key -> key.substring(RedisTargetWordKey.DAILY_WORD_PREFIX.length) }
                .forEach { chatId -> updateWordForChat(chatId) }
            if (cursor.isFinished) {
                break
            }
            cursor = userStorageConnection.sync().scan(cursor, scanArgs)
        }
        LOG.info("Finished update job")
    }

    private fun updateWordForChat(chatId: String) {
        wordRefresher.updateWordAndRefreshUsersProgresses(
            chatId,
            wordCreateStrategy.newWord()
        )
        LOG.info("Word updated for chat {}", chatId)
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(UpdateWordJob.Companion::class.java)
    }

}