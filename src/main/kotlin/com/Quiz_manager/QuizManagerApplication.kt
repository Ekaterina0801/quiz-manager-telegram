package com.Quiz_manager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class QuizManagerApplication

fun main(args: Array<String>) {
	runApplication<QuizManagerApplication>(*args)
}
