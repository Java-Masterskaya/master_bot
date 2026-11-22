package ru.yandex.masterbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** Добавляет correlation ID задачам стандартных исполнителя и планировщика Spring. */
@Configuration
public class TaskLoggingConfig {
  @Bean
  public TaskDecorator correlationTaskDecorator() {
    return CorrelationId::decorate;
  }

  @Bean
  public ThreadPoolTaskScheduler taskScheduler(TaskDecorator correlationTaskDecorator) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setTaskDecorator(correlationTaskDecorator);
    return scheduler;
  }
}
