package com.Quiz_manager.config

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class SQLiteConfig {

    @Bean
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .driverClassName("org.sqlite.JDBC")
            //.url("jdbc:sqlite:quiz-manager.db")
            .url("jdbc:sqlite:/data/quiz-manager.db")
            .build()
    }
}
