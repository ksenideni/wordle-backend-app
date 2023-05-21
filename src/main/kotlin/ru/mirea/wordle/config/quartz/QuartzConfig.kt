package ru.mirea.wordle.config.quartz

import org.quartz.JobDetail
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.CronTriggerFactoryBean
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import ru.mirea.wordle.task.UpdateWordJob


@Configuration
class QuartzConfig(
    @Value("\${task.update.cron:0 0 0 ? * * *}")
    val updateWordCronExpression: String
) {

    @Bean
    fun jobDetail(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(UpdateWordJob::class.java)
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun trigger(jobDetail: JobDetail): CronTriggerFactoryBean {
        val trigger = CronTriggerFactoryBean()
        trigger.setJobDetail(jobDetail)
        trigger.setCronExpression(updateWordCronExpression)
        return trigger
    }

}