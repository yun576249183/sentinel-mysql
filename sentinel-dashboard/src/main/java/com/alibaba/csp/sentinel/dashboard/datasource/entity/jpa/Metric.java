package com.alibaba.csp.sentinel.dashboard.datasource.entity.jpa;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @className MetricPO
 * @description 持久化到数据库实体类
 * @author Bamboo
 * @date 2019/12/10 14:56
 * @version 1.0
 */
@Data
@Entity
@Table(name = "sentinel_metric")
public class Metric implements Serializable {
    private static final long serialVersionUID = -499670661853195313L;

    /**
     * 主键
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    /**
     * 创建时间
     */
    @Column(name = "gmt_create")
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @Column(name = "gmt_modified")
    private Date gmtModified;

    /**
     * 应用名称
     */
    @Column(name = "app")
    private String app;

    /**
     * 统计时间
     */
    @Column(name = "timestamp")
    private Date timestamp;

    /**
     * 资源名称
     */
    @Column(name = "resource")
    private String resource;

    /**
     * 通过qps
     */
    @Column(name = "pass_qps")
    private Long passQps;

    /**
     * 成功qps
     */
    @Column(name = "success_qps")
    private Long successQps;

    /**
     * 限流qps
     */
    @Column(name = "block_qps")
    private Long blockQps;

    /**
     * 发送异常的次数
     */
    @Column(name = "exception_qps")
    private Long exceptionQps;

    /**
     * 所有successQps的rt的和
     */
    @Column(name = "rt")
    private Double rt;

    /**
     * 本次聚合的总条数
     */
    @Column(name = "count")
    private Integer count;

    /**
     * 资源的hashCode
     */
    @Column(name = "resource_code")
    private Integer resourceCode;
}
