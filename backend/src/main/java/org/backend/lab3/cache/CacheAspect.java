package org.backend.lab3.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Aspect
public class CacheAspect {
    private final MetricsManager metricsManager;
    @Getter
    @Setter
    private volatile boolean loggingEnabled = true;

    @Around("@annotation(org.backend.lab3.cache.TrackCache)")
    public Object logCache(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingEnabled) {
            return joinPoint.proceed();
        }
        long hitsBefore = metricsManager.getHits();
        long missesBefore = metricsManager.getMisses();
        Object result = joinPoint.proceed();
        long hits = metricsManager.getHits();
        long misses = metricsManager.getMisses();
        long hitsDelta = hits - hitsBefore;
        long missesDelta = misses - missesBefore;
        if (hitsDelta > 0 && missesDelta > 0) {
            log.info("cache hit, cache miss. total hits: {} (+{}), total misses: {} (+{})", hits, hitsDelta, misses, missesDelta);
        } else if (hitsDelta > 0) {
            log.info("cache hit. total hits: {} (+{}), total misses: {}", hits, hitsDelta, misses);
        } else if (missesDelta > 0) {
            log.info("cache miss. total hits: {}, total misses: {} (+{})", hits, misses, missesDelta);
        } else {
            log.info("cache not changed. total hits: {}, total misses: {}", hits, misses);
        }
        return result;
    }
}
