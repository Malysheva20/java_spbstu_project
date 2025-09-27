package com.example.demo.scheduler;

import com.example.demo.repository.misc.OverdueTaskNativeRepository;
import com.example.demo.entity.NotificationEntity;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("sched")
public class OverdueTaskScheduler {
  private final OverdueTaskNativeRepository overdueRepo;
  private final EntityManager em;
  public OverdueTaskScheduler(OverdueTaskNativeRepository overdueRepo, EntityManager em) {
    this.overdueRepo = overdueRepo;
    this.em = em;
  }
  @Scheduled(fixedDelayString = "${app.scheduling.overdue.fixedDelay:60000}")
  public void checkOverdue() { process(); }
  @Async
  protected void process() {
    LocalDateTime now = LocalDateTime.now();
    List<Object[]> rows = overdueRepo.findAllOverdue(now);
    for (Object[] r : rows) {
      Long userId = ((Number) r[1]).longValue();
      String title = (String) r[2];
      NotificationEntity e = new NotificationEntity();
      e.setUserId(userId);
      e.setMessage("Task overdue: " + title);
      e.setReadFlag(false);
      e.setCreatedAt(LocalDateTime.now());
      em.persist(e);
    }
  }
}

