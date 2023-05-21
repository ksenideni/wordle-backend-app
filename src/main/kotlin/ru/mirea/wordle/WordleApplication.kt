package ru.mirea.wordle

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(value = ["ru.mirea.wordle.config"])
class WordleApplication

fun main(args: Array<String>) {
    var ctx = runApplication<WordleApplication>(*args)
}
