package org.backend.lab3.cache;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

@Component
public class MetricsManager {
    private final Statistics statistics;

    public MetricsManager(EntityManagerFactory emf) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        this.statistics = sessionFactory.getStatistics();
    }

    public long getHits() {
        return statistics.getSecondLevelCacheHitCount();
    }

    public long getMisses() {
        return statistics.getSecondLevelCacheMissCount();
    }
}
