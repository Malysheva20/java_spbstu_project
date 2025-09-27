package com.example.demo.messaging;

import com.example.demo.config.RabbitConfig;
import com.example.demo.model.Task;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Aspect
@Component
@Profile("amqp")
public class TaskCreatePublishAspect {
  private final RabbitTemplate rabbitTemplate;
  public TaskCreatePublishAspect(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }
  @AfterReturning(pointcut = "execution(* com.example.demo.service..*Task*.*create(..)) && args(task)", returning = "result")
  public void publishTaskCreated(Object result, Object task) {
    if (result instanceof Task && task instanceof Task) {
      Task saved = (Task) result;
      LocalDateTime ldt = saved.getCreatedAt();
      Instant createdAt = ldt != null ? ldt.atZone(ZoneId.systemDefault()).toInstant() : Instant.now();
      TaskCreatedEvent evt = new TaskCreatedEvent(saved.getId(), saved.getUserId(), saved.getTitle(), createdAt);
      rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_TASK_CREATED, evt);
    }
  }
}

