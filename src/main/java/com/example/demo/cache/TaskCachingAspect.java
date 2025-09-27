package com.example.demo.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.concurrent.Callable;

@Aspect
@Component
@Profile("redis")
public class TaskCachingAspect {
  private final Cache getCache;
  private final Cache allCache;
  private final Cache pendingCache;
  public TaskCachingAspect(CacheManager cacheManager) {
    this.getCache = cacheManager.getCache("task:get");
    this.allCache = cacheManager.getCache("task:all");
    this.pendingCache = cacheManager.getCache("task:pending");
  }
  @Around("execution(* com.example.demo.service..*get(..)) && args(id)")
  public Object cacheGetById(ProceedingJoinPoint pjp, Object id) throws Throwable {
    if (getCache == null) return pjp.proceed();
    Object key = id;
    Object v = getCache.get(key, new Callable<Object>() { public Object call() throws Exception { try { return pjp.proceed(); } catch (Throwable t) { throw new Exception(t); } } });
    return v;
  }
  @Around("execution(* com.example.demo.service..*getAll(..)) && args(userId,..)")
  public Object cacheGetAll(ProceedingJoinPoint pjp, Object userId) throws Throwable {
    if (allCache == null) return pjp.proceed();
    Object key = userId;
    Object v = allCache.get(key, new Callable<Object>() { public Object call() throws Exception { try { return pjp.proceed(); } catch (Throwable t) { throw new Exception(t); } } });
    return v;
  }
  @Around("execution(* com.example.demo.service..*getPending(..)) && args(userId,..)")
  public Object cacheGetPending(ProceedingJoinPoint pjp, Object userId) throws Throwable {
    if (pendingCache == null) return pjp.proceed();
    Object key = userId;
    Object v = pendingCache.get(key, new Callable<Object>() { public Object call() throws Exception { try { return pjp.proceed(); } catch (Throwable t) { throw new Exception(t); } } });
    return v;
  }
  @Around("execution(* com.example.demo.service..*create(..)) && args(task)")
  public Object evictOnCreate(ProceedingJoinPoint pjp, Object task) throws Throwable {
    Object r = pjp.proceed();
    if (task != null) {
      Object userId = null;
      try { userId = task.getClass().getMethod("getUserId").invoke(task); } catch (Exception ignored) {}
      if (allCache != null && userId != null) allCache.evict(userId);
      if (pendingCache != null && userId != null) pendingCache.evict(userId);
    }
    if (r != null) {
      try { Object id = r.getClass().getMethod("getId").invoke(r); if (getCache != null && id != null) getCache.evict(id); } catch (Exception ignored) {}
    }
    return r;
  }
  @Around("execution(* com.example.demo.service..*delete(..)) && args(id)")
  public Object evictOnDelete(ProceedingJoinPoint pjp, Object id) throws Throwable {
    Object r = pjp.proceed();
    if (getCache != null && id != null) getCache.evict(id);
    if (allCache != null) allCache.clear();
    if (pendingCache != null) pendingCache.clear();
    return r;
  }
}

