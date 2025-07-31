package com.Quiz_manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();

        // Включаем логирование client info: IP и sessionId
        loggingFilter.setIncludeClientInfo(true);
        // Показываем строку запроса (?foo=bar)
        loggingFilter.setIncludeQueryString(true);
        // Показываем заголовки (можно выключить, если много чувствительного)
        loggingFilter.setIncludeHeaders(false);
        // Показываем тело запроса (payload)
        loggingFilter.setIncludePayload(true);
        // Максимальная длина тела, которое будем логировать
        loggingFilter.setMaxPayloadLength(2048);

        // К префиксу и суффиксу можно добавить свои метки
        loggingFilter.setBeforeMessagePrefix(">>> REQUEST >>> ");
        loggingFilter.setBeforeMessageSuffix("");
        loggingFilter.setAfterMessagePrefix("<<< RESPONSE <<< ");
        loggingFilter.setAfterMessageSuffix("");

        return loggingFilter;
    }
}
