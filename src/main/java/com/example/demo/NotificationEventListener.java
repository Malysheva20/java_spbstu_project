package com.example.demo.messaging;

import com.example.demo.entity.NotificationEntity;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import static com.example.demo.config.RabbitConfig.QUEUE_NOTIFICATIONS;

@Component
@Profile("amqp")
public class NotificationEventListener {
  private final EntityManager em;
  public NotificationEventListener(EntityManager em) { this.em = em; }
  @Async
  @Transactional
  @RabbitListener(queues = QUEUE_NOTIFICATIONS)
  public void onTaskCreated(TaskCreatedEvent event) {
    NotificationEntity e = new NotificationEntity();
    e.setUserId(event.getUserId());
    e.setMessage("Task created: " + event.getTitle());
    e.setReadFlag(false);
    e.setCreatedAt(LocalDateTime.now());
    em.persist(e);
  }
}

