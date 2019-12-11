package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.jpa.Metric;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @className JpaMetricsRepository
 * @description Jpa处理监控信息
 * @author Bamboo
 * @date 2019/12/10 15:15
 * @version 1.0
 */
@Repository("jpaMetricsRepository")
public class JpaMetricsRepository implements MetricsRepository<MetricEntity>{

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 单条插入监控信息
     * @auther Bamboo
     * @date 2019/12/10 15:22:48
     * @param metric    监控信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }

        Metric metricDO = new Metric();
        BeanUtils.copyProperties(metric, metricDO);
        entityManager.persist(metricDO);
    }

    /**
     * 批量插入监控
     * @auther Bamboo
     * @date 2019/12/10 15:22:48
     * @param metrics    监控信息集合
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }

        metrics.forEach(this::save);
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<MetricEntity>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        if (StringUtil.isBlank(resource)) {
            return results;
        }

        StringBuilder hql = new StringBuilder();
        hql.append("FROM Metric");
        hql.append(" WHERE app=:app");
        hql.append(" AND resource=:resource");
        hql.append(" AND timestamp>=:startTime");
        hql.append(" AND timestamp<=:endTime");

        Query query = entityManager.createQuery(hql.toString());
        query.setParameter("app", app);
        query.setParameter("resource", resource);
        query.setParameter("startTime", Date.from(Instant.ofEpochMilli(startTime)));
        query.setParameter("endTime", Date.from(Instant.ofEpochMilli(endTime)));

        List<Metric> metrics = query.getResultList();
        if (CollectionUtils.isEmpty(metrics)) {
            return results;
        }

        for (Metric metric : metrics) {
            MetricEntity metricEntity = new MetricEntity();
            BeanUtils.copyProperties(metric, metricEntity);
            results.add(metricEntity);
        }

        return results;
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        StringBuilder hql = new StringBuilder();
        hql.append("FROM Metric");
        hql.append(" WHERE app=:app");

        long startTime = System.currentTimeMillis() - 1000 * 60;
        Query query = entityManager.createQuery(hql.toString());
        query.setParameter("app", app);

        List<Metric> metrics = query.getResultList();
        if (CollectionUtils.isEmpty(metrics)) {
            return results;
        }

        List<MetricEntity> metricEntities = new ArrayList<MetricEntity>();
        for (Metric metric : metrics) {
            MetricEntity metricEntity = new MetricEntity();
            BeanUtils.copyProperties(metric, metricEntity);
            metricEntities.add(metricEntity);
        }

        Map<String, MetricEntity> resourceCount = new HashMap<>(32);

        for (MetricEntity metricEntity : metricEntities) {
            String resource = metricEntity.getResource();
            if (resourceCount.containsKey(resource)) {
                MetricEntity oldEntity = resourceCount.get(resource);
                oldEntity.addPassQps(metricEntity.getPassQps());
                oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
                oldEntity.addBlockQps(metricEntity.getBlockQps());
                oldEntity.addExceptionQps(metricEntity.getExceptionQps());
                oldEntity.addCount(1);
            } else {
                resourceCount.put(resource, MetricEntity.copyOf(metricEntity));
            }
        }

        // Order by last minute b_qps DESC.
        return resourceCount.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    MetricEntity e1 = o1.getValue();
                    MetricEntity e2 = o2.getValue();
                    int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                    if (t != 0) {
                        return t;
                    }
                    return e2.getPassQps().compareTo(e1.getPassQps());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
